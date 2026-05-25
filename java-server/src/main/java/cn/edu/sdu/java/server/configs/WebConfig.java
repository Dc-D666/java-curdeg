package cn.edu.sdu.java.server.configs;

import cn.edu.sdu.java.server.util.AttachmentStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${attach.folder}")
    private String attachFolder;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        List<String> uploadLocations = new ArrayList<>();
        for (Path root : AttachmentStorageUtil.resolveCandidateUploadRoots(attachFolder)) {
            uploadLocations.add(AttachmentStorageUtil.toResourceLocation(root));
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocations.toArray(String[]::new));
        
        registry.addResourceHandler("/images_level/**")
                .addResourceLocations("file:./images_level/");
    }
}
