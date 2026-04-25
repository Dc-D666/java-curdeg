package com.teach.javafx.request;

import com.teach.javafx.AppStore;
import com.teach.javafx.models.Board;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * HttpRequestUtil 后台请求实例程序，主要实践向后台发送请求的方法
 *  static boolean isLocal 业务处理程序实现方式 false java-server实现 前端程序通过下面的方法把数据发送后台程序，后台返回前端需要的数据，true 本地方式 业务处理 在SQLiteJDBC 实现
 *  String serverUrl = "http://localhost:9090" 后台服务的机器地址和端口号
 */
public class HttpRequestUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final HttpClient client = HttpClient.newHttpClient();
    public static String serverUrl = "http://localhost:22223";
//    public static String serverUrl = "http://202.194.7.29:22222";

    /**
     *  应用关闭是需要做关闭处理
     */
    public static void close(){
    }

    /**
     * String login(LoginRequest request)  用户登录请求实现
     * @param request  username 登录账号 password 登录密码
     * @return  返回null 登录成功 AppStore注册登录账号信息 非空，登录错误信?     */

    public static String login(LoginRequest request){
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/api/auth/login"))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .headers("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("response.statusCode===="+response.statusCode());
                if (response.statusCode() == 200) {
                    JwtResponse jwt = gson.fromJson(response.body(), JwtResponse.class);
                    AppStore.setJwt(jwt);
                    return null;
                } else if (response.statusCode() == 401) {
                    return "用户名或密码不存在！";
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        return "登录失败";
    }

    /**
     * DataResponse request(String url,DataRequest request) 一般数据请求业务的实现
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return DataResponse 返回后台返回数据
     */
    public static DataResponse<Object> request(String url, DataRequest request){
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + url))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            request.add("username",AppStore.getJwt().getUsername());
            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("url=" + url +"    response.statusCode="+response.statusCode());
                if (response.statusCode() == 200) {
                    //                System.out.println(response.body());
                    Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                    return gson.fromJson(response.body(), responseType);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        return null;
    }

    /**
     *  MyTreeNode requestTreeNode(String url, DataRequest request) 获取树节点对?     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return MyTreeNode 返回后台返回数据
     */
    public static MyTreeNode requestTreeNode(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), MyTreeNode.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MyTreeNode> requestTreeNodeList(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                List<Map<String,Object>> list = gson.fromJson(response.body(),List.class);
                List<MyTreeNode> rList = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : list) {
                    rList.add(new MyTreeNode(stringObjectMap));
                }
                return rList;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  List<OptionItem> requestOptionItemList(String url, DataRequest request) 获取OptionItemList对象
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return List<OptionItem> 返回后台返回数据
     */
    public static List<OptionItem> requestOptionItemList(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                OptionItemList o = gson.fromJson(response.body(), OptionItemList.class);
                return o.getItemList();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *   List<OptionItem> getDictionaryOptionItemList(String code) 获取数据字典OptionItemList对象
     * @param code  数据字典类型?     * @param
     * @return List<OptionItem> 返回后台返回数据
     */
    public static  List<OptionItem> getDictionaryOptionItemList(String code) {
        DataRequest req = new DataRequest();
        req.add("code", code);
        return requestOptionItemList("/api/base/getDictionaryOptionItemList",req);
    }

    /**
     *  byte[] requestByteData(String url, DataRequest request) 获取byte[] 对象 下载数据文件?     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return List<OptionItem> 返回后台返回数据
     */
    public static byte[] requestByteData(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<byte[]>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DataResponse uploadFile(String fileName,String remoteFile) 上传数据文件
     * @param fileName  本地文件?     * @param remoteFile 远程文件路径
     * @return 上传操作信息
     */
    public static DataResponse uploadFile(String uri,String fileName,String remoteFile)  {
        try {
            Path file = Path.of(fileName);
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl+uri+"?uploader=HttpTestApp&remoteFile="+remoteFile + "&fileName="
                            + file.getFileName()))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), DataResponse.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DataResponse importData(String url, String fileName, String paras) 导入数据文件
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param fileName 本地文件?     * @param paras  上传参数
     * @return 导入结果信息
     */
    public static DataResponse importData(String url, String fileName, String paras)  {
        try {
            Path file = Path.of(fileName);
            String urlStr = serverUrl+url+"?uploader=HttpTestApp&fileName=" + file.getFileName() ;
            if(paras != null && !paras.isEmpty())
                urlStr += "&"+paras;
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), DataResponse.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Board> getBoardList() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/board/list"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getBoardList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<List<Board>>>(){}.getType();
                DataResponse<List<Board>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PageResult<Post> getPostList(Long boardId, String keyword, int pageNum, int pageSize) {
        java.util.List<String> params = new java.util.ArrayList<>();
        
        if (boardId != null) {
            params.add("boardId=" + boardId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            try {
                params.add("keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8"));
            } catch (Exception e) {
                params.add("keyword=" + keyword);
            }
        }
        params.add("pageNum=" + pageNum);
        params.add("pageSize=" + pageSize);
        
        String url = serverUrl + "/api/bbs/post/list?" + String.join("&", params);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            System.out.println("getPostList request: " + url);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getPostList response: " + response.body());
            if (response.statusCode() == 200) {
                try {
                    Type responseType = new TypeToken<DataResponse<PageResult<Post>>>(){}.getType();
                    DataResponse<PageResult<Post>> dataResponse = gson.fromJson(response.body(), responseType);
                    System.out.println("Parsed dataResponse: code=" + dataResponse.getCode());
                    if (dataResponse.getCode() == 0) {
                        PageResult<Post> result = dataResponse.getData();
                        System.out.println("PageResult: total=" + result.getTotal() + ", list size=" + (result.getList() != null ? result.getList().size() : "null"));
                        return result;
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing response:");
                    e.printStackTrace();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getCurrentUser() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/user/me"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getCurrentUser response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<User>>(){}.getType();
                DataResponse<User> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User updateUserProfile(String nickname, String signature, String avatarUrl, 
                                           String personName, String personDept, String personGender, 
                                           String personBirthday, String personEmail, String personPhone, 
                                           String personAddress, String personIntroduce,
                                           String namePrivacy, String deptPrivacy, String genderPrivacy,
                                           String birthdayPrivacy, String emailPrivacy, String phonePrivacy,
                                           String addressPrivacy, String introducePrivacy) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("nickname", nickname);
        dataRequest.add("signature", signature);
        dataRequest.add("avatarUrl", avatarUrl);
        dataRequest.add("personName", personName);
        dataRequest.add("personDept", personDept);
        dataRequest.add("personGender", personGender);
        dataRequest.add("personBirthday", personBirthday);
        dataRequest.add("personEmail", personEmail);
        dataRequest.add("personPhone", personPhone);
        dataRequest.add("personAddress", personAddress);
        dataRequest.add("personIntroduce", personIntroduce);
        dataRequest.add("namePrivacy", namePrivacy);
        dataRequest.add("deptPrivacy", deptPrivacy);
        dataRequest.add("genderPrivacy", genderPrivacy);
        dataRequest.add("birthdayPrivacy", birthdayPrivacy);
        dataRequest.add("emailPrivacy", emailPrivacy);
        dataRequest.add("phonePrivacy", phonePrivacy);
        dataRequest.add("addressPrivacy", addressPrivacy);
        dataRequest.add("introducePrivacy", introducePrivacy);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/user/me"))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("updateUserProfile response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<User>>(){}.getType();
                DataResponse<User> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Post getPostDetail(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getPostDetail response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Post>>(){}.getType();
                DataResponse<Post> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Comment> getCommentList(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/comment/post/" + postId))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getCommentList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<List<Comment>>>(){}.getType();
                DataResponse<List<Comment>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Comment getCommentDetail(Long commentId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/comment/detail/" + commentId))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getCommentDetail response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Comment>>(){}.getType();
                DataResponse<Comment> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Post publishPost(Post post) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("title", post.getTitle());
        dataRequest.add("content", post.getContent());
        dataRequest.add("boardId", post.getBoardId());
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            dataRequest.add("imageUrls", post.getImages());
        }
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("publishPost response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Post>>(){}.getType();
                DataResponse<Post> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Comment publishComment(Long postId, String content) {
        return publishComment(postId, content, null);
    }

    public static Comment publishComment(Long postId, String content, Long parentId) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("content", content);
        if (parentId != null) {
            dataRequest.add("parentId", parentId);
        }
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/comment/post/" + postId))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("publishComment response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Comment>>(){}.getType();
                DataResponse<Comment> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Post updatePost(Long postId, String title, String content, String imageUrls) {
        DataRequest dataRequest = new DataRequest();
        if (title != null) {
            dataRequest.add("title", title);
        }
        if (content != null) {
            dataRequest.add("content", content);
        }
        if (imageUrls != null) {
            dataRequest.add("imageUrls", imageUrls);
        }
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("updatePost response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Post>>(){}.getType();
                DataResponse<Post> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean deletePost(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId))
                .DELETE()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("deletePost response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                return dataResponse.getCode() == 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static Post toggleTop(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/top"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("toggleTop response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Post>>(){}.getType();
                DataResponse<Post> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Post toggleFeature(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/feature"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("toggleFeature response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Post>>(){}.getType();
                DataResponse<Post> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> toggleLike(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/like"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("toggleLike response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getLikeStatus(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/like/status"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getLikeStatus response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> toggleFavorite(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/favorite"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("toggleFavorite response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getFavoriteStatus(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/favorite/status"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getFavoriteStatus response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> toggleCommentLike(Long commentId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/comment/" + commentId + "/like"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("toggleCommentLike response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getCommentLikeStatus(Long commentId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/comment/" + commentId + "/like/status"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getCommentLikeStatus response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static com.teach.javafx.models.Report submitReport(Integer targetType, Long targetId, String reason) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("targetType", targetType);
        dataRequest.add("targetId", targetId);
        dataRequest.add("reason", reason);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/report"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("submitReport response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<com.teach.javafx.models.Report>>(){}.getType();
                DataResponse<com.teach.javafx.models.Report> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PageResult<com.teach.javafx.models.Report> getMyReportList(int pageNum, int pageSize) {
        String url = serverUrl + "/api/bbs/report/my-list?pageNum=" + pageNum + "&pageSize=" + pageSize;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyReportList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<PageResult<com.teach.javafx.models.Report>>>(){}.getType();
                DataResponse<PageResult<com.teach.javafx.models.Report>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PageResult<com.teach.javafx.models.Report> getAdminReportList(int pageNum, int pageSize, Integer status) {
        java.util.List<String> params = new java.util.ArrayList<>();
        params.add("pageNum=" + pageNum);
        params.add("pageSize=" + pageSize);
        if (status != null) {
            params.add("status=" + status);
        }
        
        String url = serverUrl + "/api/bbs/report/admin-list?" + String.join("&", params);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getAdminReportList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<PageResult<com.teach.javafx.models.Report>>>(){}.getType();
                DataResponse<PageResult<com.teach.javafx.models.Report>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean handleReport(Long id, int handleType, String handleRemark) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("handleType", handleType);
        if (handleRemark != null) {
            dataRequest.add("handleRemark", handleRemark);
        }
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/report/" + id + "/handle"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("handleReport response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                return dataResponse.getCode() == 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Long getUnreadNotificationCount() {
        String url = serverUrl + "/api/bbs/notification/unread-count";
        System.out.println("getUnreadNotificationCount request URL: " + url);
        System.out.println("getUnreadNotificationCount has JWT token: " + (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null));
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUnreadNotificationCount response status: " + response.statusCode());
            System.out.println("getUnreadNotificationCount response body: " + response.body());
            
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Long>>(){}.getType();
                DataResponse<Long> dataResponse = gson.fromJson(response.body(), responseType);
                
                if (dataResponse != null) {
                    System.out.println("getUnreadNotificationCount dataResponse code: " + dataResponse.getCode());
                    
                    if (dataResponse.getCode() == 0) {
                        return dataResponse.getData();
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("getUnreadNotificationCount exception:");
            e.printStackTrace();
        }
        return 0L;
    }

    public static java.util.List<com.teach.javafx.models.Notification> getMyNotificationList(Integer isRead, Integer type) {
        String url = serverUrl + "/api/bbs/notification/my-list";
        java.util.List<String> params = new java.util.ArrayList<>();
        
        if (isRead != null) {
            params.add("isRead=" + isRead);
        }
        if (type != null) {
            params.add("type=" + type);
        }
        
        if (!params.isEmpty()) {
            url += "?" + String.join("&", params);
        }
        
        System.out.println("getMyNotificationList request URL: " + url);
        System.out.println("getMyNotificationList has JWT token: " + (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null));
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyNotificationList response status: " + response.statusCode());
            System.out.println("getMyNotificationList response body: " + response.body());
            
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<com.teach.javafx.models.Notification>>>(){}.getType();
                DataResponse<java.util.List<com.teach.javafx.models.Notification>> dataResponse = gson.fromJson(response.body(), responseType);
                
                if (dataResponse != null) {
                    System.out.println("getMyNotificationList dataResponse code: " + dataResponse.getCode());
                    System.out.println("getMyNotificationList dataResponse msg: " + dataResponse.getMsg());
                    
                    if (dataResponse.getCode() == 0) {
                        return dataResponse.getData();
                    }
                } else {
                    System.out.println("getMyNotificationList dataResponse is null after deserialization!");
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("getMyNotificationList exception:");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean readNotification(Long id) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/notification/" + id + "/read"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("readNotification response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                return dataResponse.getCode() == 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean markAllNotificationsAsRead() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/notification/mark-all-read"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("markAllNotificationsAsRead response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                return dataResponse.getCode() == 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static java.util.List<java.util.Map<String, Object>> getDailyPostStatistics() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/statistics/daily-post"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getDailyPostStatistics response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<java.util.Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<java.util.Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<com.teach.javafx.models.Post> getHotPostStatistics() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/statistics/hot-post"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getHotPostStatistics response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<com.teach.javafx.models.Post>>>(){}.getType();
                DataResponse<java.util.List<com.teach.javafx.models.Post>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<com.teach.javafx.models.User> getActiveUserStatistics() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/statistics/active-user"))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getActiveUserStatistics response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<com.teach.javafx.models.User>>>(){}.getType();
                DataResponse<java.util.List<com.teach.javafx.models.User>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> toggleFollow(Long followingId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/toggle/" + followingId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("toggleFollow response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> checkFollowStatus(Long followingId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/status/" + followingId))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("checkFollowStatus response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getMyFollowingList() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/my-following"))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyFollowingList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<List<User>>>(){}.getType();
                DataResponse<List<User>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getMyFollowerList() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/my-follower"))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyFollowerList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<List<User>>>(){}.getType();
                DataResponse<List<User>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getUserFollowingList(Integer userId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/following/" + userId))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserFollowingList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<List<User>>>(){}.getType();
                DataResponse<List<User>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getUserFollowerList(Integer userId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/follower/" + userId))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserFollowerList response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<List<User>>>(){}.getType();
                DataResponse<List<User>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String uploadImage(String filePath) {
        try {
            Path file = Path.of(filePath);
            String boundary = "---" + UUID.randomUUID().toString();
            String newline = "\r\n";
            
            byte[] fileBytes = Files.readAllBytes(file);
            String fileName = file.getFileName().toString();
            
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            
            outputStream.write(("--" + boundary + newline).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + newline).getBytes());
            outputStream.write(("Content-Type: image/jpeg" + newline + newline).getBytes());
            outputStream.write(fileBytes);
            outputStream.write(newline.getBytes());
            outputStream.write(("--" + boundary + "--" + newline).getBytes());
            
            byte[] requestBody = outputStream.toByteArray();
            
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/api/bbs/file/upload-image"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .headers("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
                builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
            }
            
            HttpRequest httpRequest = builder.build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("uploadImage response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<String>>(){}.getType();
                DataResponse<String> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> uploadImage(Path file) {
        try {
            String boundary = "---" + UUID.randomUUID().toString();
            String newline = "\r\n";
            
            byte[] fileBytes = Files.readAllBytes(file);
            String fileName = file.getFileName().toString();
            
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            
            outputStream.write(("--" + boundary + newline).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + newline).getBytes());
            outputStream.write(("Content-Type: image/jpeg" + newline + newline).getBytes());
            outputStream.write(fileBytes);
            outputStream.write(newline.getBytes());
            outputStream.write(("--" + boundary + "--" + newline).getBytes());
            
            byte[] requestBody = outputStream.toByteArray();
            
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/api/bbs/file/upload-image"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .headers("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
                builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
            }
            
            HttpRequest httpRequest = builder.build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("uploadImage response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String registerUser(String studentId, String nickname, String email, String password, String emailCode) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("username", studentId);
        dataRequest.add("perName", nickname);
        dataRequest.add("email", email);
        dataRequest.add("password", password);
        dataRequest.add("emailCode", emailCode);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/auth/registerUser"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json")
                .build();
        
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("registerUser response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return null;
                } else {
                    return dataResponse.getMsg() != null ? dataResponse.getMsg() : "注册失败";
                }
            } else if (response.statusCode() == 400) {
                return "注册信息无效";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "注册失败";
    }
    
    public static String sendEmailCode(String email) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("email", email);
        dataRequest.add("type", "REGISTER");
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/auth/sendEmailCode"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json")
                .build();
        
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("sendEmailCode response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return null;
                } else {
                    return dataResponse.getMsg() != null ? dataResponse.getMsg() : "发送失败";
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "发送失败";
    }

    public static Map<String, Object> getUserStatistics() {
        String url = serverUrl + "/api/bbs/user/me/statistics";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserStatistics response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

public static PageResult<Post> getMyPosts(int page, int size) {
    String url = serverUrl + "/api/bbs/user/me/posts?pageNum=" + page + "&pageSize=" + size;

    HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .headers("Content-Type", "application/json");

    if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
        builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
    }

    HttpRequest httpRequest = builder.build();
    try {
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("getMyPosts response: " + response.body());
        if (response.statusCode() == 200) {
            Type responseType = new TypeToken<DataResponse<PageResult<Post>>>(){}.getType();
            DataResponse<PageResult<Post>> dataResponse = gson.fromJson(response.body(), responseType);
            if (dataResponse.getCode() == 0) {
                return dataResponse.getData();
            }
        }
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
    return null;
}

public static PageResult<Post> getMyFavorites(int page, int size) {
    String url = serverUrl + "/api/bbs/user/me/favorites?pageNum=" + page + "&pageSize=" + size;

    HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .headers("Content-Type", "application/json");

    if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
        builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
    }

    HttpRequest httpRequest = builder.build();
    try {
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("getMyFavorites response: " + response.body());
        if (response.statusCode() == 200) {
            Type responseType = new TypeToken<DataResponse<PageResult<Post>>>(){}.getType();
            DataResponse<PageResult<Post>> dataResponse = gson.fromJson(response.body(), responseType);
            if (dataResponse.getCode() == 0) {
                return dataResponse.getData();
            }
        }
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
    return null;
}

    public static String changePassword(String oldPassword, String newPassword) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("oldPassword", oldPassword);
        dataRequest.add("newPassword", newPassword);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/user/me/password"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("changePassword response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return null;
                } else {
                    return dataResponse.getMsg() != null ? dataResponse.getMsg() : "修改密码失败";
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "修改密码失败";
    }

    public static Map<String, Object> getMyFollowingPage(int pageNum, int pageSize) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/my-following/page?pageNum=" + pageNum + "&pageSize=" + pageSize))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyFollowingPage response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getMyFollowerPage(int pageNum, int pageSize) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/follow/my-follower/page?pageNum=" + pageNum + "&pageSize=" + pageSize))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyFollowerPage response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getUserProfile(Integer userId) {
        String url = serverUrl + "/api/bbs/user/" + userId;
        System.out.println("getUserProfile request URL: " + url);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserProfile response status: " + response.statusCode());
            System.out.println("getUserProfile response body: " + response.body());
            
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                
                if (dataResponse != null) {
                    System.out.println("getUserProfile dataResponse code: " + dataResponse.getCode());
                    
                    if (dataResponse.getCode() == 0) {
                        return dataResponse.getData();
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("getUserProfile exception:");
            e.printStackTrace();
        }
        return null;
    }

    public static PageResult<Post> getAdminPendingPosts(int pageNum, int pageSize) {
        java.util.List<String> params = new java.util.ArrayList<>();
        params.add("pageNum=" + pageNum);
        params.add("pageSize=" + pageSize);

        String url = serverUrl + "/api/admin/moderation/pending?" + String.join("&", params);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            System.out.println("getAdminPendingPosts request: " + url);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getAdminPendingPosts response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<PageResult<Post>>>(){}.getType();
                DataResponse<PageResult<Post>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PageResult<Post> getAdminAllPosts(int pageNum, int pageSize) {
        System.out.println("===== getAdminAllPosts 开始 =====");
        java.util.List<String> params = new java.util.ArrayList<>();
        params.add("pageNum=" + pageNum);
        params.add("pageSize=" + pageSize);

        String url = serverUrl + "/api/admin/moderation/posts?" + String.join("&", params);
        System.out.println("请求URL: " + url);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            System.out.println("发送请求...");
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("收到响应，状态码: " + response.statusCode());
            System.out.println("响应内容: " + response.body());
            
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<PageResult<Post>>>(){}.getType();
                DataResponse<PageResult<Post>> dataResponse = gson.fromJson(response.body(), responseType);
                System.out.println("解析结果: code=" + dataResponse.getCode() + 
                    ", msg=" + dataResponse.getMsg());
                
                if (dataResponse.getCode() == 0) {
                    PageResult<Post> data = dataResponse.getData();
                    if (data != null) {
                        System.out.println("数据条数: " + (data.getList() != null ? data.getList().size() : 0) + 
                            ", 总数: " + data.getTotal());
                    }
                    return data;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("===== getAdminAllPosts 异常 =====");
            e.printStackTrace();
        }
        System.out.println("===== getAdminAllPosts 返回 null =====");
        return null;
    }

    public static boolean moderatePost(Long postId, String decision, String violationLevel, String violationType, String remark) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("decision", decision);
        if (violationLevel != null) {
            dataRequest.add("violationLevel", violationLevel);
        }
        if (violationType != null) {
            dataRequest.add("violationType", violationType);
        }
        if (remark != null) {
            dataRequest.add("remark", remark);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/admin/moderation/" + postId + "/review"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            String url = serverUrl + "/api/admin/moderation/" + postId + "/review";
            System.out.println("moderatePost request: " + url + ", decision: " + decision);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("moderatePost response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Object>>(){}.getType();
                DataResponse<Object> dataResponse = gson.fromJson(response.body(), responseType);
                return dataResponse.getCode() == 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static PageResult<Post> searchPosts(String keyword, String searchType, int pageNum, int pageSize) {
        java.util.List<String> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            try {
                params.add("keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8"));
            } catch (Exception e) {
                params.add("keyword=" + keyword);
            }
        }
        if (searchType != null && !searchType.isEmpty()) {
            params.add("searchType=" + searchType);
        }
        params.add("pageNum=" + pageNum);
        params.add("pageSize=" + pageSize);

        String url = serverUrl + "/api/bbs/post/search?" + String.join("&", params);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            System.out.println("searchPosts request: " + url);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("searchPosts response: " + response.body());
            if (response.statusCode() == 200) {
                try {
                    Type responseType = new TypeToken<DataResponse<PageResult<Post>>>(){}.getType();
                    DataResponse<PageResult<Post>> dataResponse = gson.fromJson(response.body(), responseType);
                    System.out.println("Parsed dataResponse: code=" + dataResponse.getCode());
                    if (dataResponse.getCode() == 0) {
                        PageResult<Post> result = dataResponse.getData();
                        System.out.println("PageResult: total=" + result.getTotal() + ", list size=" + (result.getList() != null ? result.getList().size() : "null"));
                        return result;
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing response:");
                    e.printStackTrace();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getOverview() {
        String url = serverUrl + "/api/bbs/statistics/overview";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getOverview response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getUserGrowth(int days) {
        String url = serverUrl + "/api/bbs/statistics/user-growth?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserGrowth response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getUserTypeDistribution() {
        String url = serverUrl + "/api/bbs/statistics/user-type";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserTypeDistribution response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<User> getActiveUsers(String sortBy) {
        String url = serverUrl + "/api/bbs/statistics/active-users?sortBy=" + sortBy;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getActiveUsers response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<User>>>(){}.getType();
                DataResponse<java.util.List<User>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getUserActivityDistribution() {
        String url = serverUrl + "/api/bbs/statistics/user-activity";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getUserActivityDistribution response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getBannedUsers() {
        String url = serverUrl + "/api/bbs/statistics/banned-users";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getBannedUsers response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getPostTrend(int days) {
        String url = serverUrl + "/api/bbs/statistics/post-trend?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getPostTrend response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getBoardDistribution() {
        String url = serverUrl + "/api/bbs/statistics/board-distribution";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getBoardDistribution response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Post> getHotPosts(String sortBy) {
        String url = serverUrl + "/api/bbs/statistics/hot-posts?sortBy=" + sortBy;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getHotPosts response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Post>>>(){}.getType();
                DataResponse<java.util.List<Post>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getPostStatusDistribution() {
        String url = serverUrl + "/api/bbs/statistics/post-status";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getPostStatusDistribution response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getImagePostRatio() {
        String url = serverUrl + "/api/bbs/statistics/image-post-ratio";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getImagePostRatio response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getCommentTrend(int days) {
        String url = serverUrl + "/api/bbs/statistics/comment-trend?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getCommentTrend response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getLikeTrend(int days) {
        String url = serverUrl + "/api/bbs/statistics/like-trend?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getLikeTrend response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getFavoriteTrend(int days) {
        String url = serverUrl + "/api/bbs/statistics/favorite-trend?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getFavoriteTrend response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getFollowTrend(int days) {
        String url = serverUrl + "/api/bbs/statistics/follow-trend?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getFollowTrend response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getHotComments() {
        String url = serverUrl + "/api/bbs/statistics/hot-comments";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getHotComments response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getModerationOverview() {
        String url = serverUrl + "/api/bbs/statistics/moderation-overview";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getModerationOverview response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getModerationTrend(int days) {
        String url = serverUrl + "/api/bbs/statistics/moderation-trend?days=" + days;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getModerationTrend response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getViolationTypeDistribution() {
        String url = serverUrl + "/api/bbs/statistics/violation-types";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getViolationTypeDistribution response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<java.util.List<Map<String, Object>>>>(){}.getType();
                DataResponse<java.util.List<Map<String, Object>>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getReportStatistics() {
        String url = serverUrl + "/api/bbs/statistics/report-statistics";
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getReportStatistics response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getReportStats() {
        return getReportStatistics();
    }

    public static Map<String, Object> aiSearch(String keyword) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("keyword", keyword);
        DataResponse<Object> response = request("/api/bbs/post/ai-search", dataRequest);
        if (response != null && response.getCode() == 0) {
            return (Map<String, Object>) response.getData();
        }
        return null;
    }

    public static Map<String, Object> getPostSummary(Long postId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/" + postId + "/summary"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("Content-Type", "application/json");

        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }

        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getPostSummary response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<Map<String, Object>>>(){}.getType();
                DataResponse<Map<String, Object>> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void aiSearchStream(String keyword, 
            java.util.function.Consumer<List<com.teach.javafx.models.Post>> onPosts, 
            java.util.function.Consumer<String> onContent, 
            java.util.function.Consumer<String> onError, 
            Runnable onComplete) {
        
        new Thread(() -> {
            java.io.InputStream inputStream = null;
            try {
                String url = serverUrl + "/api/bbs/post/ai-search-stream?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8");
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .headers("Accept", "text/event-stream");
                
                if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
                    builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
                }
                
                HttpRequest httpRequest = builder.build();
                
                // 使用 InputStream 实现真正的流式处理！
                HttpResponse<java.io.InputStream> response = client.send(httpRequest, 
                        HttpResponse.BodyHandlers.ofInputStream());
                
                // 检查响应状态码
                if (response.statusCode() != 200) {
                    onError.accept("服务器响应异常: " + response.statusCode());
                    return;
                }
                
                inputStream = response.body();
                // ⚠️ 关键：前端也不用 BufferedReader 缓冲！直接无缓冲读取！
                java.io.InputStreamReader isr = new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
                final boolean[] isCompleted = {false};
                long startTime = System.currentTimeMillis();
                int[] chunkCounter = {0};
                StringBuilder lineBuffer = new StringBuilder();
                
                System.out.println("[前端] 开始读取SSE响应流...");
                
                int c;
                while ((c = isr.read()) != -1) {
                    if (c == '\n') {
                        // 读完一行
                        String line = lineBuffer.toString();
                        lineBuffer.setLength(0); // 清空 buffer
                        
                        try {
                            if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                
                                if ("[DONE]".equals(data)) {
                                    long time = System.currentTimeMillis() - startTime;
                                    System.out.println("[前端 " + time + "ms] 收到 [DONE]！");
                                    if (!isCompleted[0]) {
                                        isCompleted[0] = true;
                                        onComplete.run();
                                    }
                                    continue;
                                }
                                
                                if (!data.isEmpty()) {
                                    try {
                                        com.google.gson.JsonObject jsonObject = gson.fromJson(data, com.google.gson.JsonObject.class);
                                        String type = jsonObject.get("type").getAsString();
                                        chunkCounter[0]++;
                                        long time = System.currentTimeMillis() - startTime;
                                        
                                        if ("posts".equals(type)) {
                                            System.out.println("[前端 " + time + "ms] #"+chunkCounter[0]+" 收到 posts事件");
                                            Type listType = new TypeToken<List<com.teach.javafx.models.Post>>(){}.getType();
                                            List<com.teach.javafx.models.Post> posts = gson.fromJson(jsonObject.get("data"), listType);
                                            onPosts.accept(posts);
                                        } else if ("content".equals(type)) {
                                            String content = jsonObject.get("data").getAsString();
                                            System.out.println("[前端 " + time + "ms] #"+chunkCounter[0]+" 收到 content: '" + content + "'");
                                            onContent.accept(content);
                                        } else if ("done".equals(type)) {
                                            System.out.println("[前端 " + time + "ms] #"+chunkCounter[0]+" 收到 done事件");
                                            if (!isCompleted[0]) {
                                                isCompleted[0] = true;
                                                onComplete.run();
                                            }
                                        } else if ("error".equals(type)) {
                                            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "未知错误";
                                            System.out.println("[前端 " + time + "ms] 收到 error: " + message);
                                            onError.accept(message);
                                        }
                                    } catch (Exception e) {
                                        System.err.println("[前端] 解析data失败: " + e.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[前端] 处理line失败: " + e.getMessage());
                        }
                    } else if (c != '\r') {
                        // 忽略 \r，只追加其他字符
                        lineBuffer.append((char) c);
                    }
                }
                
                System.out.println("[前端] SSE响应流读取结束！共收到" + chunkCounter[0] + "个事件");
                
            } catch (java.io.IOException e) {
                // 连接关闭异常 - 如果是正常完成则忽略
                String msg = e.getMessage();
                if (msg == null || (!msg.contains("closed") && !msg.contains("EOF") && !msg.contains("reset"))) {
                    onError.accept("网络连接异常: " + e.getMessage());
                }
            } catch (Exception e) {
                onError.accept(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }).start();
    }

    public static com.teach.javafx.models.AiWriteResponse aiWrite(String title, String content, String instruction, String operation) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("title", title);
        dataRequest.add("content", content);
        dataRequest.add("instruction", instruction);
        dataRequest.add("operation", operation);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/post/ai-write"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("aiWrite response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<com.teach.javafx.models.AiWriteResponse>>(){}.getType();
                DataResponse<com.teach.javafx.models.AiWriteResponse> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse != null && dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                } else if (dataResponse != null) {
                    // 返回失败信息
                    com.teach.javafx.models.AiWriteResponse errorResponse = new com.teach.javafx.models.AiWriteResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage(dataResponse.getMsg());
                    return errorResponse;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        // 返回通用错误
        com.teach.javafx.models.AiWriteResponse errorResponse = new com.teach.javafx.models.AiWriteResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("AI写作调用失败");
        return errorResponse;
    }
    
    public static com.teach.javafx.models.AiImageResponse aiImageGenerate(String prompt, String size) {
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("prompt", prompt);
        dataRequest.add("size", size);
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/api/bbs/ai-image/generate"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataRequest)))
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null && AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("aiImageGenerate response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken<DataResponse<com.teach.javafx.models.AiImageResponse>>(){}.getType();
                DataResponse<com.teach.javafx.models.AiImageResponse> dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse != null && dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                } else if (dataResponse != null) {
                    // 返回失败信息
                    com.teach.javafx.models.AiImageResponse errorResponse = new com.teach.javafx.models.AiImageResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage(dataResponse.getMsg());
                    return errorResponse;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        // 返回通用错误
        com.teach.javafx.models.AiImageResponse errorResponse = new com.teach.javafx.models.AiImageResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("AI图片生成调用失败");
        return errorResponse;
    }
}
