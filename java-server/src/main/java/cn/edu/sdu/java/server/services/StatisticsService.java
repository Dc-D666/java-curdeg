package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StatisticsService {
    private final UserRepository userRepository;
    private final StatisticsDayRepository statisticsDayRepository;
    private final BbsPostRepository bbsPostRepository;
    private final BbsCommentRepository bbsCommentRepository;
    private final BbsLikeRepository bbsLikeRepository;
    private final BbsFavoriteRepository bbsFavoriteRepository;
    private final BbsFollowRepository bbsFollowRepository;
    private final BbsReportRepository bbsReportRepository;
    private final BbsModerationLogRepository bbsModerationLogRepository;
    private final BbsBoardRepository bbsBoardRepository;

    public StatisticsService(UserRepository userRepository, StatisticsDayRepository statisticsDayRepository,
                             BbsPostRepository bbsPostRepository, BbsCommentRepository bbsCommentRepository,
                             BbsLikeRepository bbsLikeRepository, BbsFavoriteRepository bbsFavoriteRepository,
                             BbsFollowRepository bbsFollowRepository, BbsReportRepository bbsReportRepository,
                             BbsModerationLogRepository bbsModerationLogRepository, BbsBoardRepository bbsBoardRepository) {
        this.userRepository = userRepository;
        this.statisticsDayRepository = statisticsDayRepository;
        this.bbsPostRepository = bbsPostRepository;
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsLikeRepository = bbsLikeRepository;
        this.bbsFavoriteRepository = bbsFavoriteRepository;
        this.bbsFollowRepository = bbsFollowRepository;
        this.bbsReportRepository = bbsReportRepository;
        this.bbsModerationLogRepository = bbsModerationLogRepository;
        this.bbsBoardRepository = bbsBoardRepository;
    }

    public DataResponse getMainPageData(DataRequest dataRequest) {
        Date day = new Date();
        Date monthDay = DateTimeTool.prevMonth(day);
        int i;
        Integer id;
        Object[] a;
        Long l;
        String name;
        long total = userRepository.count();
        Integer monthCount = userRepository.countLastLoginTime(DateTimeTool.parseDateTime(monthDay,"yyyy-MM-dd")+" 00:00:00");
        Integer dayCount = userRepository.countLastLoginTime(DateTimeTool.parseDateTime(day,"yyyy-MM-dd")+" 00:00:00");
        Map<String,Object> data = new HashMap<>();
        Map<String,Object> m = new HashMap<>();
        m.put("total", (int) total);
        m.put("monthCount",monthCount);
        m.put("dayCount",dayCount);
        data.put("onlineUser", m);
        List<?> nList = userRepository.getCountList();
        List<Map<String,Object>> userTypeList = new ArrayList<>();
        for(i= 0;i < nList.size();i++) {
            m = new HashMap<>();
            a = (Object[])nList.get(i);
            id = (Integer)a[0];
            l = (Long)a[1];
            if(id == 1)
                name = "管理员";
            else if(id == 2)
                name = "学生";
            else if(id == 3)
                name = "教师";
            else
                name = "";
            m.put("name", name);
            m.put("value",l.intValue());
            userTypeList.add(m);
        }
        data.put("userTypeList", userTypeList);
        List<StatisticsDay>sList = statisticsDayRepository.findListByDay(DateTimeTool.parseDateTime(monthDay,"yyyyMMdd"),DateTimeTool.parseDateTime(day,"yyyyMMdd"));
        List<String> dayList = new ArrayList<>();
        List<String> lList = new ArrayList<>();
        List<String> rList = new ArrayList<>();
        List<String> cList = new ArrayList<>();
        List<String> mList = new ArrayList<>();
        for(StatisticsDay s:sList) {
            dayList.add(s.getDay());
            lList.add(""+s.getLoginCount());
            rList.add(""+s.getRequestCount());
            cList.add(""+s.getCreateCount());
            mList.add(""+s.getLoginCount());
        }
        m = new HashMap<>();
        m.put("value",dayList);
        m.put("label1",lList);
        m.put("label2",rList);
        data.put("requestData", m);
        m = new HashMap<>();
        m.put("value",dayList);
        m.put("label1",cList);
        m.put("label2",mList);
        data.put("operateData", m);

        return CommonMethod.getReturnData(data);
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userRepository.countTotalUsers());
        result.put("todayNewUsers", userRepository.countTodayNewUsers());
        result.put("monthlyActiveUsers", userRepository.countMonthlyActiveUsers());
        result.put("postCount", bbsPostRepository.countTotalPosts());
        result.put("todayNewPosts", bbsPostRepository.countTodayPosts());
        result.put("commentCount", bbsCommentRepository.countTotalComments());
        result.put("todayNewComments", bbsCommentRepository.countTodayComments());
        result.put("pendingModerationCount", bbsPostRepository.countPendingModerationPosts());
        return result;
    }

    public List<Map<String, Object>> getUserGrowthTrend(Integer days) {
        List<Object[]> rawData = userRepository.countDailyUserGrowth(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getUserTypeDistribution() {
        List<?> nList = userRepository.getCountList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object obj : nList) {
            Object[] a = (Object[]) obj;
            Integer id = (Integer) a[0];
            Long l = (Long) a[1];
            String name;
            if (id == 1) name = "超级管理员";
            else if (id == 2) name = "管理员";
            else if (id == 3) name = "普通用户";
            else name = "";
            Map<String, Object> m = new HashMap<>();
            m.put("name", name);
            m.put("count", l.intValue());
            result.add(m);
        }
        return result;
    }

    public List<User> getActiveUsers(String sortBy) {
        List<User> users;
        if ("posts".equals(sortBy)) {
            users = userRepository.findTop20ByIsBannedOrderByPostCountDesc(false);
        } else if ("comments".equals(sortBy)) {
            users = userRepository.findTop20ByIsBannedOrderByCommentCountDesc(false);
        } else {
            users = userRepository.findTop10ByIsBannedOrderByPostCountDescCommentCountDesc(false);
        }
        for (User user : users) {
            user.setPassword(null);
        }
        return users;
    }

    public List<Map<String, Object>> getUserActivityDistribution() {
        List<Object[]> rawData = userRepository.countUserActivityDistribution();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getBannedUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", userRepository.countBannedUsers());
        List<User> users = userRepository.findByIsBanned(true);
        for (User user : users) {
            user.setPassword(null);
        }
        result.put("list", users);
        return result;
    }

    public List<Map<String, Object>> getPostTrend(Integer days) {
        List<Object[]> rawData = bbsPostRepository.countDailyPostTrend(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getBoardDistribution() {
        List<Object[]> rawData = bbsPostRepository.countPostsByBoard();
        List<BbsBoard> boards = bbsBoardRepository.findAll();
        Map<Long, String> boardNameMap = new HashMap<>();
        for (BbsBoard board : boards) {
            boardNameMap.put(board.getId(), board.getName());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            Long boardId = ((Number) row[0]).longValue();
            item.put("name", boardNameMap.getOrDefault(boardId, "未知"));
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<BbsPost> getHotPosts(String sortBy) {
        List<BbsPost> posts;
        if ("likes".equals(sortBy)) {
            posts = bbsPostRepository.findTop20ByStatusOrderByLikeCountDesc(1);
        } else if ("comments".equals(sortBy)) {
            posts = bbsPostRepository.findTop20ByStatusOrderByCommentCountDesc(1);
        } else if ("views".equals(sortBy)) {
            posts = bbsPostRepository.findTop20ByStatusOrderByViewCountDesc(1);
        } else if ("favorites".equals(sortBy)) {
            posts = bbsPostRepository.findTop20ByStatusOrderByFavoriteCountDesc(1);
        } else {
            posts = bbsPostRepository.findTop10ByStatusOrderByLikeCountDescCommentCountDesc(1);
        }
        for (BbsPost post : posts) {
            if (post.getAuthorId() != null) {
                Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                authorOptional.ifPresent(user -> post.setAuthorNickname(user.getNickname()));
            }
        }
        return posts;
    }

    public List<Map<String, Object>> getPostStatusDistribution() {
        List<Object[]> rawData = bbsPostRepository.countPostsByStatus();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "审核通过");
        statusMap.put(2, "审核中");
        statusMap.put(3, "违规");
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            Integer status;
            if (row[0] instanceof Boolean) {
                status = ((Boolean) row[0]) ? 1 : 0;
            } else if (row[0] instanceof Number) {
                status = ((Number) row[0]).intValue();
            } else {
                status = 0;
            }
            if (statusMap.containsKey(status)) {
                item.put("name", statusMap.get(status));
                item.put("count", row[1]);
                result.add(item);
            }
        }
        return result;
    }

    public List<Map<String, Object>> getImagePostRatio() {
        List<Object[]> rawData = bbsPostRepository.countPostsByImageStatus();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("with_image", "含图帖");
        typeMap.put("without_image", "纯文帖");
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            String type = (String) row[0];
            item.put("type", type);
            item.put("typeName", typeMap.getOrDefault(type, "未知"));
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getCommentTrend(Integer days) {
        List<Object[]> rawData = bbsCommentRepository.countDailyCommentTrend(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getLikeTrend(Integer days) {
        List<Object[]> rawData = bbsLikeRepository.countDailyLikeTrend(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getFavoriteTrend(Integer days) {
        List<Object[]> rawData = bbsFavoriteRepository.countDailyFavoriteTrend(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getFollowTrend(Integer days) {
        List<Object[]> rawData = bbsFollowRepository.countDailyFollowTrend(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getHotComments() {
        List<BbsComment> comments = bbsCommentRepository.findTop10ByStatusOrderByLikeCountDesc(1);
        List<Map<String, Object>> result = new ArrayList<>();
        for (BbsComment comment : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", comment.getContent() != null ? comment.getContent() : "");
            item.put("likeCount", comment.getLikeCount() != null ? comment.getLikeCount() : 0);
            item.put("commentTime", comment.getCreateTime() != null ? comment.getCreateTime() : "");
            
            String author = "";
            if (comment.getAuthorId() != null) {
                Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
                if (authorOptional.isPresent()) {
                    author = authorOptional.get().getNickname() != null ? authorOptional.get().getNickname() : "";
                }
            }
            item.put("author", author);
            
            String postTitle = "";
            if (comment.getPostId() != null) {
                Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
                if (postOptional.isPresent()) {
                    postTitle = postOptional.get().getTitle() != null ? postOptional.get().getTitle() : "";
                }
            }
            item.put("postTitle", postTitle);
            
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getModerationOverview() {
        Map<String, Object> result = new HashMap<>();
        Long totalModerations = bbsModerationLogRepository.countTotalModerations();
        Long pendingPosts = bbsPostRepository.countPendingModerationPosts();
        Long passedCount = bbsModerationLogRepository.countPassedModerations();
        Long rejectedCount = bbsModerationLogRepository.countRejectedModerations();
        result.put("totalModeration", totalModerations);
        result.put("pendingCount", pendingPosts);

        double passRateValue = 0.0;
        if (totalModerations > 0) {
            passRateValue = (double) passedCount / totalModerations * 100;
        }
        double rejectRateValue = 0.0;
        if (totalModerations > 0) {
            rejectRateValue = (double) rejectedCount / totalModerations * 100;
        }
        result.put("passRate", Math.round(passRateValue * 100.0) / 100.0);
        result.put("rejectRate", Math.round(rejectRateValue * 100.0) / 100.0);
        return result;
    }

    public List<Map<String, Object>> getModerationTrend(Integer days) {
        List<Object[]> rawData = bbsModerationLogRepository.countDailyModerationTrend(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("passed", row[1]);
            item.put("rejected", row[2]);
            item.put("pending", row[3]);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getViolationTypeDistribution() {
        List<Object[]> rawData = bbsModerationLogRepository.countViolationTypes();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> typeNameMap = new HashMap<>();
        typeNameMap.put("OTHER", "其他");
        typeNameMap.put("INAPPROPRIATE_CONTENT", "不适内容");
        typeNameMap.put("SPAM", "垃圾信息");
        typeNameMap.put("FRAUD", "诈骗信息");
        typeNameMap.put("ABUSE", "滥用内容");
        
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            String typeName = (String) row[0];
            item.put("name", typeNameMap.getOrDefault(typeName, typeName));
            item.put("count", row[1]);
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getReportStatistics() {
        Map<String, Object> result = new HashMap<>();
        Long totalReports = bbsReportRepository.countTotalReports();
        Long pendingReports = bbsReportRepository.countPendingReports();
        Long validReports = bbsReportRepository.countValidReports();
        result.put("totalReports", totalReports);
        result.put("pendingReports", pendingReports);
        result.put("validReports", validReports);
        double handleRateValue = totalReports > 0 ? (double)(totalReports - pendingReports) / totalReports * 100 : 0.0;
        result.put("handleRate", Math.round(handleRateValue * 100.0) / 100.0);
        return result;
    }
}

