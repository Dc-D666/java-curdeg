package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/export")
public class ExportController {

    private final BbsPostRepository bbsPostRepository;
    private final UserRepository userRepository;

    public ExportController(BbsPostRepository bbsPostRepository, UserRepository userRepository) {
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts")
    public DataResponse exportPosts(@RequestBody DataRequest dataRequest) {
        try {
            List<String> fields = new ArrayList<>();
            List<?> fieldList = dataRequest.getList("fields");
            for (Object f : fieldList) {
                fields.add(f.toString());
            }

            String scope = dataRequest.getString("scope");
            Long boardId = dataRequest.getLong("boardId");
            boolean includeDeleted = dataRequest.getBoolean("includeDeleted");
            String format = dataRequest.getString("format");
            if (format == null || format.isBlank()) format = "CSV";

            List<BbsPost> posts = bbsPostRepository.findAll();

            if ("BOARD".equals(scope) && boardId != null) {
                posts = posts.stream()
                        .filter(p -> boardId.equals(p.getBoardId()))
                        .toList();
            }

            if (!includeDeleted) {
                posts = posts.stream()
                        .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                        .toList();
            }

            byte[] result;
            switch (format) {
                case "EXCEL" -> result = generatePostsExcel(posts, fields);
                case "JSON" -> result = generatePostsJson(posts, fields);
                default -> result = generatePostsCsv(posts, fields);
            }

            String base64 = Base64.getEncoder().encodeToString(result);
            return CommonMethod.getReturnData(base64);
        } catch (Exception e) {
            log.error("导出帖子数据失败", e);
            return CommonMethod.getReturnMessageError("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/users")
    public DataResponse exportUsers(@RequestBody DataRequest dataRequest) {
        try {
            List<String> fields = new ArrayList<>();
            List<?> fieldList = dataRequest.getList("fields");
            for (Object f : fieldList) {
                fields.add(f.toString());
            }

            String format = dataRequest.getString("format");
            if (format == null || format.isBlank()) format = "CSV";

            List<User> users = userRepository.findAll();

            byte[] result;
            switch (format) {
                case "EXCEL" -> result = generateUsersExcel(users, fields);
                case "JSON" -> result = generateUsersJson(users, fields);
                default -> result = generateUsersCsv(users, fields);
            }

            String base64 = Base64.getEncoder().encodeToString(result);
            return CommonMethod.getReturnData(base64);
        } catch (Exception e) {
            log.error("导出用户数据失败", e);
            return CommonMethod.getReturnMessageError("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/statistics")
    public DataResponse exportStatistics(@RequestBody DataRequest dataRequest) {
        try {
            String format = dataRequest.getString("format");
            if (format == null || format.isBlank()) format = "CSV";

            long totalPosts = bbsPostRepository.count();
            long totalUsers = userRepository.count();
            Long todayPosts = bbsPostRepository.countTodayPosts();
            Long pendingModeration = bbsPostRepository.countPendingModerationPosts();

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalPosts", totalPosts);
            stats.put("totalUsers", totalUsers);
            stats.put("todayPosts", todayPosts != null ? todayPosts : 0);
            stats.put("pendingModeration", pendingModeration != null ? pendingModeration : 0);

            List<Map<String, Object>> boardDist = new ArrayList<>();
            List<Object[]> boardData = bbsPostRepository.countPostsByBoard();
            for (Object[] row : boardData) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("boardId", row[0]);
                item.put("count", row[1]);
                boardDist.add(item);
            }
            stats.put("boardDistribution", boardDist);

            List<Map<String, Object>> statusDist = new ArrayList<>();
            List<Object[]> statusData = bbsPostRepository.countPostsByStatus();
            for (Object[] row : statusData) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("status", row[0]);
                item.put("count", row[1]);
                statusDist.add(item);
            }
            stats.put("statusDistribution", statusDist);

            byte[] result;
            if ("JSON".equals(format)) {
                result = generateStatisticsJson(stats);
            } else {
                result = generateStatisticsCsv(stats);
            }

            String base64 = Base64.getEncoder().encodeToString(result);
            return CommonMethod.getReturnData(base64);
        } catch (Exception e) {
            log.error("导出统计数据失败", e);
            return CommonMethod.getReturnMessageError("导出失败: " + e.getMessage());
        }
    }

    private Map<String, String> getPostFieldHeaders() {
        Map<String, String> h = new LinkedHashMap<>();
        h.put("id", "ID");
        h.put("title", "标题");
        h.put("content", "内容");
        h.put("authorId", "作者ID");
        h.put("boardId", "板块ID");
        h.put("likeCount", "点赞数");
        h.put("commentCount", "评论数");
        h.put("createTime", "创建时间");
        h.put("updateTime", "更新时间");
        h.put("isTop", "是否置顶");
        h.put("isFeatured", "是否精选");
        h.put("status", "状态");
        return h;
    }

    private String getPostFieldValue(BbsPost post, String field) {
        return switch (field) {
            case "id" -> post.getId() != null ? post.getId().toString() : "";
            case "title" -> post.getTitle() != null ? post.getTitle() : "";
            case "content" -> post.getContent() != null ? post.getContent() : "";
            case "authorId" -> post.getAuthorId() != null ? post.getAuthorId().toString() : "";
            case "boardId" -> post.getBoardId() != null ? post.getBoardId().toString() : "";
            case "likeCount" -> post.getLikeCount() != null ? post.getLikeCount().toString() : "0";
            case "commentCount" -> post.getCommentCount() != null ? post.getCommentCount().toString() : "0";
            case "createTime" -> post.getCreateTime() != null ? post.getCreateTime() : "";
            case "updateTime" -> post.getUpdateTime() != null ? post.getUpdateTime() : "";
            case "isTop" -> post.getIsTop() != null ? post.getIsTop().toString() : "false";
            case "isFeatured" -> post.getIsFeatured() != null ? post.getIsFeatured().toString() : "false";
            case "status" -> post.getStatus() != null ? post.getStatus().toString() : "";
            default -> "";
        };
    }

    private byte[] generatePostsCsv(List<BbsPost> posts, List<String> fields) {
        Map<String, String> fieldHeaders = getPostFieldHeaders();
        List<String> exportFields = fields.isEmpty() ? new ArrayList<>(fieldHeaders.keySet()) : fields;
        StringBuilder sb = new StringBuilder();
        List<String> headers = new ArrayList<>();
        for (String field : exportFields) {
            headers.add(fieldHeaders.getOrDefault(field, field));
        }
        sb.append(String.join(",", headers)).append("\n");
        for (BbsPost post : posts) {
            List<String> values = new ArrayList<>();
            for (String field : exportFields) {
                values.add(escapeCsv(getPostFieldValue(post, field)));
            }
            sb.append(String.join(",", values)).append("\n");
        }
        return withBom(sb.toString());
    }

    private byte[] generatePostsExcel(List<BbsPost> posts, List<String> fields) throws Exception {
        Map<String, String> fieldHeaders = getPostFieldHeaders();
        List<String> exportFields = fields.isEmpty() ? new ArrayList<>(fieldHeaders.keySet()) : fields;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("帖子数据");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < exportFields.size(); i++) {
                headerRow.createCell(i).setCellValue(fieldHeaders.getOrDefault(exportFields.get(i), exportFields.get(i)));
            }
            for (int r = 0; r < posts.size(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < exportFields.size(); c++) {
                    row.createCell(c).setCellValue(getPostFieldValue(posts.get(r), exportFields.get(c)));
                }
            }
            for (int i = 0; i < exportFields.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generatePostsJson(List<BbsPost> posts, List<String> fields) {
        Map<String, String> fieldHeaders = getPostFieldHeaders();
        List<String> exportFields = fields.isEmpty() ? new ArrayList<>(fieldHeaders.keySet()) : fields;
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < posts.size(); i++) {
            sb.append("  {");
            for (int j = 0; j < exportFields.size(); j++) {
                String key = exportFields.get(j);
                String value = getPostFieldValue(posts.get(i), key);
                if (j > 0) sb.append(", ");
                sb.append("\"").append(key).append("\": \"").append(escapeJson(value)).append("\"");
            }
            sb.append("}");
            if (i < posts.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Map<String, String> getUserFieldHeaders() {
        Map<String, String> h = new LinkedHashMap<>();
        h.put("personId", "用户ID");
        h.put("userName", "用户名");
        h.put("nickname", "昵称");
        h.put("studentId", "学号");
        h.put("postCount", "发帖数");
        h.put("commentCount", "评论数");
        h.put("isBanned", "是否禁言");
        h.put("createTime", "创建时间");
        return h;
    }

    private String getUserFieldValue(User user, String field) {
        return switch (field) {
            case "personId" -> user.getPersonId() != null ? user.getPersonId().toString() : "";
            case "userName" -> user.getUserName() != null ? user.getUserName() : "";
            case "nickname" -> user.getNickname() != null ? user.getNickname() : "";
            case "studentId" -> user.getStudentId() != null ? user.getStudentId() : "";
            case "postCount" -> user.getPostCount() != null ? user.getPostCount().toString() : "0";
            case "commentCount" -> user.getCommentCount() != null ? user.getCommentCount().toString() : "0";
            case "isBanned" -> user.getIsBanned() != null ? user.getIsBanned().toString() : "false";
            case "createTime" -> user.getCreateTime() != null ? user.getCreateTime() : "";
            default -> "";
        };
    }

    private byte[] generateUsersCsv(List<User> users, List<String> fields) {
        Map<String, String> fieldHeaders = getUserFieldHeaders();
        List<String> exportFields = fields.isEmpty() ? new ArrayList<>(fieldHeaders.keySet()) : fields;
        StringBuilder sb = new StringBuilder();
        List<String> headers = new ArrayList<>();
        for (String field : exportFields) {
            headers.add(fieldHeaders.getOrDefault(field, field));
        }
        sb.append(String.join(",", headers)).append("\n");
        for (User user : users) {
            List<String> values = new ArrayList<>();
            for (String field : exportFields) {
                values.add(escapeCsv(getUserFieldValue(user, field)));
            }
            sb.append(String.join(",", values)).append("\n");
        }
        return withBom(sb.toString());
    }

    private byte[] generateUsersExcel(List<User> users, List<String> fields) throws Exception {
        Map<String, String> fieldHeaders = getUserFieldHeaders();
        List<String> exportFields = fields.isEmpty() ? new ArrayList<>(fieldHeaders.keySet()) : fields;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("用户数据");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < exportFields.size(); i++) {
                headerRow.createCell(i).setCellValue(fieldHeaders.getOrDefault(exportFields.get(i), exportFields.get(i)));
            }
            for (int r = 0; r < users.size(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < exportFields.size(); c++) {
                    row.createCell(c).setCellValue(getUserFieldValue(users.get(r), exportFields.get(c)));
                }
            }
            for (int i = 0; i < exportFields.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateUsersJson(List<User> users, List<String> fields) {
        Map<String, String> fieldHeaders = getUserFieldHeaders();
        List<String> exportFields = fields.isEmpty() ? new ArrayList<>(fieldHeaders.keySet()) : fields;
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < users.size(); i++) {
            sb.append("  {");
            for (int j = 0; j < exportFields.size(); j++) {
                String key = exportFields.get(j);
                String value = getUserFieldValue(users.get(i), key);
                if (j > 0) sb.append(", ");
                sb.append("\"").append(key).append("\": \"").append(escapeJson(value)).append("\"");
            }
            sb.append("}");
            if (i < users.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateStatisticsCsv(Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("指标,数值\n");
        sb.append("总帖子数,").append(stats.get("totalPosts")).append("\n");
        sb.append("总用户数,").append(stats.get("totalUsers")).append("\n");
        sb.append("今日发帖数,").append(stats.get("todayPosts")).append("\n");
        sb.append("待审核帖子数,").append(stats.get("pendingModeration")).append("\n");
        sb.append("\n板块分布\n板块ID,帖子数\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> boardDist = (List<Map<String, Object>>) stats.get("boardDistribution");
        if (boardDist != null) {
            for (Map<String, Object> item : boardDist) {
                sb.append(item.get("boardId")).append(",").append(item.get("count")).append("\n");
            }
        }
        sb.append("\n状态分布\n状态,帖子数\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statusDist = (List<Map<String, Object>>) stats.get("statusDistribution");
        if (statusDist != null) {
            for (Map<String, Object> item : statusDist) {
                sb.append(item.get("status")).append(",").append(item.get("count")).append("\n");
            }
        }
        return withBom(sb.toString());
    }

    private byte[] generateStatisticsJson(Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"totalPosts\": ").append(stats.get("totalPosts")).append(",\n");
        sb.append("  \"totalUsers\": ").append(stats.get("totalUsers")).append(",\n");
        sb.append("  \"todayPosts\": ").append(stats.get("todayPosts")).append(",\n");
        sb.append("  \"pendingModeration\": ").append(stats.get("pendingModeration")).append(",\n");
        sb.append("  \"boardDistribution\": [\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> boardDist = (List<Map<String, Object>>) stats.get("boardDistribution");
        if (boardDist != null) {
            for (int i = 0; i < boardDist.size(); i++) {
                Map<String, Object> item = boardDist.get(i);
                sb.append("    {\"boardId\": ").append(item.get("boardId")).append(", \"count\": ").append(item.get("count")).append("}");
                if (i < boardDist.size() - 1) sb.append(",");
                sb.append("\n");
            }
        }
        sb.append("  ],\n  \"statusDistribution\": [\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statusDist = (List<Map<String, Object>>) stats.get("statusDistribution");
        if (statusDist != null) {
            for (int i = 0; i < statusDist.size(); i++) {
                Map<String, Object> item = statusDist.get(i);
                sb.append("    {\"status\": \"").append(escapeJson(String.valueOf(item.get("status")))).append("\", \"count\": ").append(item.get("count")).append("}");
                if (i < statusDist.size() - 1) sb.append(",");
                sb.append("\n");
            }
        }
        sb.append("  ]\n}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] withBom(String text) {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + bytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(bytes, 0, result, bom.length, bytes.length);
        return result;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
