package cn.edu.sdu.java.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWriteResponse {
    
    private String title;
    
    private String content;
    
    private String instructionSuggestion;
    
    private boolean success;
    
    private String message;
    
    public static AiWriteResponse success(String title, String content, String instructionSuggestion) {
        return AiWriteResponse.builder()
                .title(title)
                .content(content)
                .instructionSuggestion(instructionSuggestion)
                .success(true)
                .build();
    }
    
    public static AiWriteResponse error(String message) {
        return AiWriteResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
