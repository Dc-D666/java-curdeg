package cn.edu.sdu.java.server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWriteRequest {
    
    private String title;
    
    private String content;
    
    private String instruction;
    
    private String operation;
}
