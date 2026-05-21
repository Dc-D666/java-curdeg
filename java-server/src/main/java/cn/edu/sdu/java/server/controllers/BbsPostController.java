
package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.payload.request.AiWriteRequest;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.AiWriteResponse;
import cn.edu.sdu.java.server.payload.response.ContentSummaryResponse;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.AiSearchService;
import cn.edu.sdu.java.server.services.AiWriteService;
import cn.edu.sdu.java.server.services.BbsPostService;
import cn.edu.sdu.java.server.services.BbsUserService;
import cn.edu.sdu.java.server.services.ContentSummaryService;
import cn.edu.sdu.java.server.services.LevelPrivilegeService;
import cn.edu.sdu.java.server.util.CommonMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/post")
public class BbsPostController {

    private final BbsPostService bbsPostService;
    private final AiSearchService aiSearchService;
    private final ContentSummaryService contentSummaryService;
    private final AiWriteService aiWriteService;
    private final BbsUserService bbsUserService;
    private final LevelPrivilegeService levelPrivilegeService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    public BbsPostController(BbsPostService bbsPostService, AiSearchService aiSearchService, ContentSummaryService contentSummaryService, AiWriteService aiWriteService, BbsUserService bbsUserService, LevelPrivilegeService levelPrivilegeService, ObjectMapper objectMapper) {
        this.bbsPostService = bbsPostService;
        this.aiSearchService = aiSearchService;
        this.contentSummaryService = contentSummaryService;
        this.aiWriteService = aiWriteService;
        this.bbsUserService = bbsUserService;
        this.levelPrivilegeService = levelPrivilegeService;
        this.objectMapper = objectMapper;
        this.executorService = Executors.newCachedThreadPool();
    }

    @GetMapping("/list")
    public DataResponse getPostList(@RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach((key, value) -> {
            try {
                if ("pageNum".equals(key) || "pageSize".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else if ("boardId".equals(key)) {
                    dataRequest.add(key, Long.parseLong(value));
                } else {
                    dataRequest.add(key, value);
                }
            } catch (Exception e) {
                dataRequest.add(key, value);
            }
        });
        return bbsPostService.getPostList(dataRequest);
    }

    @GetMapping("/{id}")
    public DataResponse getPostDetail(@PathVariable Long id) {
        return bbsPostService.getPostDetail(id);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public DataResponse createPost(@Valid @RequestBody DataRequest dataRequest) {
        return bbsPostService.createPost(dataRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse updatePost(@PathVariable Long id, @Valid @RequestBody DataRequest dataRequest) {
        return bbsPostService.updatePost(id, dataRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse deletePost(@PathVariable Long id) {
        return bbsPostService.deletePost(id);
    }

    @PostMapping("/{id}/top")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse toggleTop(@PathVariable Long id) {
        return bbsPostService.toggleTop(id);
    }

    @PostMapping("/{id}/feature")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse toggleFeature(@PathVariable Long id) {
        return bbsPostService.toggleFeature(id);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public DataResponse toggleLike(@PathVariable Long id) {
        return bbsPostService.toggleLike(id);
    }

    @GetMapping("/{id}/like/status")
    public DataResponse getLikeStatus(@PathVariable Long id) {
        return bbsPostService.getLikeStatus(id);
    }

    @GetMapping("/{id}/likers")
    public DataResponse getPostLikers(@PathVariable Long id) {
        return bbsPostService.getPostLikers(id);
    }

    @PostMapping("/{id}/favorite")
    @PreAuthorize("isAuthenticated()")
    public DataResponse toggleFavorite(@PathVariable Long id) {
        return bbsPostService.toggleFavorite(id);
    }

    @GetMapping("/{id}/favorite/status")
    public DataResponse getFavoriteStatus(@PathVariable Long id) {
        return bbsPostService.getFavoriteStatus(id);
    }

    @GetMapping("/search")
    public DataResponse searchPosts(@RequestParam Map<String, String> params) {
        String keyword = params.get("keyword");
        String searchType = params.get("searchType");
        Integer pageNum = null;
        Integer pageSize = null;

        try {
            if (params.containsKey("pageNum")) {
                pageNum = Integer.parseInt(params.get("pageNum"));
            }
            if (params.containsKey("pageSize")) {
                pageSize = Integer.parseInt(params.get("pageSize"));
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误，使用默认值
        }

        return bbsPostService.searchPosts(keyword, searchType, pageNum, pageSize);
    }

    @PostMapping("/ai-search")
    @PreAuthorize("isAuthenticated()")
    public DataResponse aiSearch(@Valid @RequestBody DataRequest dataRequest) {
        try {
            String keyword = dataRequest.getString("keyword");
            Integer userId = CommonMethod.getPersonId();
            Object result = aiSearchService.aiSearch(keyword, userId);
            return CommonMethod.getReturnData(result);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError(e.getMessage());
        }
    }

    @PostMapping("/{postId}/summary")
    @PreAuthorize("isAuthenticated()")
    public DataResponse summarizePost(@PathVariable Long postId) {
        log.info("收到帖子总结请求，postId={}", postId);
        try {
            Integer userId = CommonMethod.getPersonId();
            ContentSummaryResponse summary = contentSummaryService.summarizePostWithComments(postId, userId);
            if (summary.isSuccess()) {
                return CommonMethod.getReturnData(summary, "总结成功");
            } else {
                return CommonMethod.getReturnMessageError(summary.getMessage());
            }
        } catch (Exception e) {
            log.error("帖子总结请求处理失败，postId={}", postId, e);
            return CommonMethod.getReturnMessageError("总结失败：" + e.getMessage());
        }
    }

    @GetMapping(value = "/ai-search-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter aiSearchStream(@RequestParam String keyword) {
        log.info("收到AI流式搜索请求，keyword={}", keyword);

        Integer userId = CommonMethod.getPersonId();
        if (userId != null) {
            try {
                cn.edu.sdu.java.server.models.User user = bbsUserService.getUserById(userId);
                int searchLimit = levelPrivilegeService.getAiSearchLimit(user.getLevel());
                if (searchLimit <= 0) {
                    SseEmitter emitter = new SseEmitter();
                    executorService.execute(() -> {
                        try {
                            sendSseEvent(emitter, objectMapper.writeValueAsString(Map.of(
                                "type", "error",
                                "message", "您当前等级不具备AI搜索权限"
                            )));
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    });
                    return emitter;
                }
                if (!levelPrivilegeService.checkAiUsageLimit(userId, "search", searchLimit)) {
                    SseEmitter emitter = new SseEmitter();
                    executorService.execute(() -> {
                        try {
                            sendSseEvent(emitter, objectMapper.writeValueAsString(Map.of(
                                "type", "error",
                                "message", "您今日AI搜索次数已达上限(" + searchLimit + "次)，请明日再来"
                            )));
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    });
                    return emitter;
                }
            } catch (RuntimeException e) {
                log.warn("获取用户信息失败: {}", e.getMessage());
            }
        }

        // 超时时间设置为120秒
        SseEmitter emitter = new SseEmitter(120000L);
        
        // 注册回调处理连接关闭
        emitter.onCompletion(() -> log.debug("SSE连接已完成"));
        emitter.onTimeout(() -> log.warn("SSE连接超时"));
        emitter.onError((ex) -> log.warn("SSE连接出错: {}", ex.getMessage()));
        
        executorService.execute(() -> {
            try {
                // 1. 先搜索相关帖子
                log.debug("开始搜索相关帖子...");
                List<BbsPost> posts = aiSearchService.searchPostsOnly(keyword);
                log.debug("搜索到 {} 个相关帖子", posts.size());
                
                // 2. 发送帖子事件
                log.debug("发送帖子列表事件...");
                Map<String, Object> postsEvent = Map.of(
                    "type", "posts",
                    "data", posts
                );
                String postsJson = objectMapper.writeValueAsString(postsEvent);
                sendSseEvent(emitter, postsJson);

                // 3. 如果有帖子，构建带评论的列表和Prompt
                List<AiSearchService.PostWithComments> postWithCommentsList = null;
                String prompt = null;
                if (!posts.isEmpty()) {
                    postWithCommentsList = aiSearchService.buildPostWithComments(posts);
                    prompt = aiSearchService.buildPrompt(keyword, postWithCommentsList);
                } else {
                    prompt = aiSearchService.buildPrompt(keyword, List.of());
                }

                // 4. 流式调用AI API
                log.debug("开始流式调用AI API...");
                long sseStartTime = System.currentTimeMillis();
                int[] sseCounter = {0};
                
                // 内容缓冲机制相关变量
                StringBuilder contentBuffer = new StringBuilder();
                StringBuilder fullContentBuffer = new StringBuilder(); // 保存完整内容
                ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
                final boolean[] flushScheduled = {false};
                final Runnable[] flushTask = new Runnable[1];
                
                // 刷新缓冲内容的方法
                Runnable flushBuffer = () -> {
                    synchronized (contentBuffer) {
                        if (contentBuffer.length() > 0) {
                            try {
                                long timeSinceStart = System.currentTimeMillis() - sseStartTime;
                                sseCounter[0]++;
                                String bufferedContent = contentBuffer.toString();
                                // 在发送前移除 SUGGEST_POST 标签及其内容
                                String cleanedContent = aiSearchService.sanitizeAiOutput(removeSuggestPostTags(bufferedContent));
                                log.debug("[SSE {}ms] #{} 准备发送缓冲内容 (原始长度={}, 清理后长度={})", 
                                    timeSinceStart, sseCounter[0], bufferedContent.length(), cleanedContent.length());
                                // 只有清理后还有内容才发送
                                if (!cleanedContent.isEmpty()) {
                                    Map<String, Object> contentEvent = Map.of(
                                        "type", "content",
                                        "data", cleanedContent
                                    );
                                    String contentJson = objectMapper.writeValueAsString(contentEvent);
                                    sendSseEvent(emitter, contentJson);
                                    log.debug("[SSE {}ms] #{} 发送缓冲内容成功！", timeSinceStart, sseCounter[0]);
                                } else {
                                    log.debug("[SSE {}ms] #{} 清理后无内容，跳过发送", timeSinceStart, sseCounter[0]);
                                }
                                contentBuffer.setLength(0);
                            } catch (Exception e) {
                                log.warn("发送SSE内容事件失败: {}", e.getMessage(), e);
                            }
                        }
                        flushScheduled[0] = false;
                    }
                };
                
                aiSearchService.callAiApiStream(
                    prompt,
                    content -> {
                        synchronized (contentBuffer) {
                            contentBuffer.append(content);
                            fullContentBuffer.append(content); // 同时保存完整内容
                            log.trace("[Buffer] 收到新内容: '{}', 当前缓冲长度: {}", content, contentBuffer.length());
                            
                            // 如果没有安排刷新任务，则安排一个
                            if (!flushScheduled[0]) {
                                flushScheduled[0] = true;
                                scheduler.schedule(flushBuffer, 80, TimeUnit.MILLISECONDS);
                                log.trace("[Buffer] 安排80ms后刷新缓冲");
                            }
                        }
                    },
                    () -> {
                        try {
                            // 先刷新剩余的缓冲内容
                            synchronized (contentBuffer) {
                                if (contentBuffer.length() > 0) {
                                    log.debug("[Buffer] done事件前刷新剩余缓冲内容");
                                    flushBuffer.run();
                                }
                            }
                            
                            // 检查完整内容是否包含发帖建议 - 总是发送
                            String fullContent = aiSearchService.sanitizeAiOutput(fullContentBuffer.toString());
                            Map<String, String> suggestPostData = parseSuggestPost(fullContent);
                            
                            if (suggestPostData != null) {
                                log.info("检测到发帖建议，发送suggest_post事件");
                                Map<String, Object> suggestPostEvent = Map.of(
                                    "type", "suggest_post",
                                    "title", suggestPostData.get("title"),
                                    "content", suggestPostData.get("content"),
                                    "hasRelatedPosts", !posts.isEmpty()  // 新增：标记是否有相关帖子
                                );
                                String suggestPostJson = objectMapper.writeValueAsString(suggestPostEvent);
                                sendSseEvent(emitter, suggestPostJson);
                            }
                            
                            scheduler.shutdown();
                            log.debug("[SSE {}ms] 准备发送done事件", System.currentTimeMillis() - sseStartTime);
                            Map<String, Object> doneEvent = Map.of("type", "done");
                            String doneJson = objectMapper.writeValueAsString(doneEvent);
                            sendSseEvent(emitter, doneJson);
                            emitter.complete();
                            log.info("AI流式搜索完成！共发送{}个内容块，耗时{}ms", sseCounter[0], System.currentTimeMillis() - sseStartTime);
                        } catch (Exception e) {
                            log.warn("发送SSE完成事件失败: {}", e.getMessage(), e);
                            scheduler.shutdown();
                            try { emitter.completeWithError(e); } catch (Exception ignored) {}
                        }
                    },
                    ex -> {
                        log.error("AI流式搜索出错: {}", ex.getMessage(), ex);
                        try {
                            // 先刷新剩余的缓冲内容
                            synchronized (contentBuffer) {
                                if (contentBuffer.length() > 0) {
                                    log.debug("[Buffer] error事件前刷新剩余缓冲内容");
                                    flushBuffer.run();
                                }
                            }
                            
                            scheduler.shutdown();
                            Map<String, Object> errorEvent = Map.of(
                                "type", "error",
                                "message", "搜索出错: " + ex.getMessage()
                            );
                            String errorJson = objectMapper.writeValueAsString(errorEvent);
                            sendSseEvent(emitter, errorJson);
                        } catch (Exception ioException) {
                            log.warn("发送SSE错误事件失败: {}", ioException.getMessage());
                        } finally {
                            scheduler.shutdown();
                            try { emitter.completeWithError(ex); } catch (Exception ignored) {}
                        }
                    }
                );

            } catch (Exception e) {
                log.error("处理AI流式搜索时发生异常: {}", e.getMessage(), e);
                try {
                    Map<String, Object> errorEvent = Map.of(
                        "type", "error",
                        "message", "服务器出错: " + e.getMessage()
                    );
                    sendSseEvent(emitter, objectMapper.writeValueAsString(errorEvent));
                } catch (Exception ioException) {
                    log.warn("发送SSE错误事件失败: {}", ioException.getMessage());
                } finally {
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }
            }
        });

        return emitter;
    }
    
    /**
     * 解析 AI 回复中的发帖建议
     */
    private Map<String, String> parseSuggestPost(String content) {
        try {
            String pattern = "\\[SUGGEST_POST\\]\\s*标题：(.*?)\\s*内容：(.*?)\\s*\\[/SUGGEST_POST\\]";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(content);
            if (m.find()) {
                String title = m.group(1).trim();
                String postContent = m.group(2).trim();
                return java.util.Map.of(
                    "title", title,
                    "content", postContent
                );
            }
        } catch (Exception e) {
            log.warn("解析发帖建议失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从内容中移除 SUGGEST_POST 标签及其包裹的内容
     */
    private String removeSuggestPostTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        try {
            String pattern = "\\[SUGGEST_POST\\]\\s*标题：.*?\\s*内容：.*?\\s*\\[/SUGGEST_POST\\]";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(content);
            String result = m.replaceAll("");
            // 如果替换后只剩余空白字符，返回空字符串
            if (result.trim().isEmpty()) {
                return "";
            }
            return result;
        } catch (Exception e) {
            log.warn("移除 SUGGEST_POST 标签失败: {}", e.getMessage());
            return content;
        }
    }
    
    /**
     * 发送SSE事件并立即flush
     */
    private void sendSseEvent(SseEmitter emitter, String data) throws IOException {
        emitter.send(SseEmitter.event().data(data).name("message"));
    }

    @PostMapping("/ai-write")
    @PreAuthorize("isAuthenticated()")
    public DataResponse aiWrite(@Valid @RequestBody DataRequest dataRequest) {
        log.info("===== 收到AI写作请求 =====");
        String title = dataRequest.getString("title");
        String content = dataRequest.getString("content");
        String instruction = dataRequest.getString("instruction");
        String operation = dataRequest.getString("operation");
        log.info("title={}", title);
        log.info("content={}", content);
        log.info("instruction={}", instruction);
        log.info("operation={}", operation);
        
        AiWriteRequest request = AiWriteRequest.builder()
            .title(title)
            .content(content)
            .instruction(instruction)
            .operation(operation)
            .build();
        
        try {
            AiWriteResponse result = aiWriteService.aiWrite(request);
            if (result.isSuccess()) {
                return CommonMethod.getReturnData(result, "AI写作完成");
            } else {
                return CommonMethod.getReturnMessageError(result.getMessage());
            }
        } catch (Exception e) {
            log.error("AI写作请求处理失败", e);
            return CommonMethod.getReturnMessageError("AI写作失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public DataResponse getUserPosts(@PathVariable Long userId, @RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach(dataRequest::add);
        return bbsPostService.getUserPosts(userId, dataRequest);
    }
}
