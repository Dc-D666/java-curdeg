package cn.edu.sdu.java.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSummaryResponse {
    
    private String postSummary;
    private String commentHotspots;
    private boolean success;
    private String message;
    
    public static ContentSummaryResponse success(String postSummary, String commentHotspots) {
        return ContentSummaryResponse.builder()
                .postSummary(postSummary)
                .commentHotspots(commentHotspots)
                .success(true)
                .build();
    }
    
    public static ContentSummaryResponse info(String message) {
        return ContentSummaryResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    public static ContentSummaryResponse error(String message) {
        return ContentSummaryResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
