
package cn.edu.sdu.java.server.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiImageRequest {
    
    private String prompt;
    
    private String size;
}
