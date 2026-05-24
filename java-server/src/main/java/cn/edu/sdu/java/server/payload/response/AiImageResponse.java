
package cn.edu.sdu.java.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiImageResponse {
    
    private String imageUrl;
    
    private boolean success;
    
    private String message;
    
    public static AiImageResponse success(String imageUrl) {
        return AiImageResponse.builder()
                .imageUrl(imageUrl)
                .success(true)
                .build();
    }
    
    public static AiImageResponse error(String message) {
        return AiImageResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
