package cn.edu.sdu.java.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class DirectAiTest {
    public static void main(String[] args) throws Exception {
        String url = "https://xplt.sdu.edu.cn:4000/v1/chat/completions";
        String token = "sk-oXWyxru5nwdf-iDN6a8sJw"; // 正确token！
        
        String requestBody = "{" +
            "\"model\": \"Ali-dashscope/Qwen3.5-Flash\"," +
            "\"messages\": [{\"role\": \"user\", \"content\": \"你好！\"}]," +
            "\"stream\": true" +
            "}";
        
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();
        
        System.out.println("🔗 [DirectTest] 发送请求...");
        long startTime = System.currentTimeMillis();
        
        HttpResponse<java.io.InputStream> response = client.send(request, 
            HttpResponse.BodyHandlers.ofInputStream());
        
        System.out.println("✅ [DirectTest] 连接成功！耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        
        // ⚠️ 直接逐字符读取！不缓冲！
        java.io.InputStream inputStream = response.body();
        java.io.InputStreamReader isr = new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8);
        
        StringBuilder lineBuffer = new StringBuilder();
        int c;
        int chunkCount = 0;
        
        System.out.println("📡 [DirectTest] 开始逐字符读取...");
        
        while ((c = isr.read()) != -1) {
            long now = System.currentTimeMillis() - startTime;
            
            // 先打印每个读取到的字符！
            System.out.print((char) c);
            
            if (c == '\n') {
                String line = lineBuffer.toString();
                lineBuffer.setLength(0);
                
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if (data.contains("content")) {
                        chunkCount++;
                        System.out.println("\n🎉 [" + now + "ms] Chunk #" + chunkCount + " 包含内容！");
                    }
                }
            } else if (c != '\r') {
                lineBuffer.append((char) c);
            }
        }
        
        System.out.println("\n🏁 [DirectTest] 全部结束！总耗时: " + (System.currentTimeMillis() - startTime) + "ms");
    }
}
