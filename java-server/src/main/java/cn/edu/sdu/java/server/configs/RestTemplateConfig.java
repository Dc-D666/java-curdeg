
package cn.edu.sdu.java.server.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ModerationConfig moderationConfig) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(moderationConfig.getApi().getTimeout());
        factory.setReadTimeout(moderationConfig.getApi().getTimeout());
        return new RestTemplate(factory);
    }
}
