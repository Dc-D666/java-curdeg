import re

with open(r'c:\Users\33031\Documents\xwechat_files\wxid_0bowm1ekyr2s22_5e3e\msg\file\2026-04\java-curdeg-0422\java-fx\src\main\java\com\teach\javafx\request\HttpRequestUtil.java', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# 使用正则表达式匹配并替换
pattern = re.compile(r'public static Map&lt;String, Object&gt; getMyPosts\([^)]*\)\s*\{[\s\S]*?return null;\s*\}', re.MULTILINE)

new_method = '''    public static PageResult&lt;Post&gt; getMyPosts(int pageNum, int pageSize) {
        String url = serverUrl + "/api/bbs/user/me/posts?pageNum=" + pageNum + "&amp;pageSize=" + pageSize;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("Content-Type", "application/json");
        
        if (AppStore.getJwt() != null &amp;&amp; AppStore.getJwt().getToken() != null) {
            builder.headers("Authorization", "Bearer " + AppStore.getJwt().getToken());
        }
        
        HttpRequest httpRequest = builder.build();
        try {
            HttpResponse&lt;String&gt; response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("getMyPosts response: " + response.body());
            if (response.statusCode() == 200) {
                Type responseType = new TypeToken&lt;DataResponse&lt;PageResult&lt;Post&gt;&gt;&gt;(){}.getType();
                DataResponse&lt;PageResult&lt;Post&gt;&gt; dataResponse = gson.fromJson(response.body(), responseType);
                if (dataResponse.getCode() == 0) {
                    return dataResponse.getData();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }'''

content = pattern.sub(new_method, content)

with open(r'c:\Users\33031\Documents\xwechat_files\wxid_0bowm1ekyr2s22_5e3e\msg\file\2026-04\java-curdeg-0422\java-fx\src\main\java\com\teach\javafx\request\HttpRequestUtil.java', 'w', encoding='utf-8', errors='ignore') as f:
    f.write(content)

print('Done!')
