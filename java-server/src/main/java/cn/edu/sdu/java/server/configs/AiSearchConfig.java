
package cn.edu.sdu.java.server.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai-search.keyword-refactor")
public class AiSearchConfig {
    private boolean enabled = false;
    private int keywordCount = 3;
    private int resultCount = 5;
    private int timeout = 30000;
}

