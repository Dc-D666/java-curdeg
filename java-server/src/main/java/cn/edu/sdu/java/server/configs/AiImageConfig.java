
package cn.edu.sdu.java.server.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai-image")
public class AiImageConfig {
    private boolean enabled = true;
    private ApiConfig api = new ApiConfig();

    @Getter
    @Setter
    public static class ApiConfig {
        private String url;
        private String key;
        private String model;
        private int timeout = 30000;
    }
}
