package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.util.DateTimeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class BbsFileService {
    private static final Logger log = LoggerFactory.getLogger(BbsFileService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String UPLOAD_FOLDER = "./uploads/";

    public String uploadImage(MultipartFile file) {
        log.info("Starting image upload, originalFilename: {}, size: {}", file.getOriginalFilename(), file.getSize());
        
        try {
            if (file.isEmpty()) {
                log.error("File is empty");
                return null;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                log.error("Original filename is null");
                return null;
            }

            String extension = getFileExtension(originalFilename);
            log.info("File extension: {}", extension);
            
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                log.error("Invalid file extension: {}", extension);
                return null;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                log.error("File too large: {} bytes, max allowed: {} bytes", file.getSize(), MAX_FILE_SIZE);
                return null;
            }

            String dateFolder = DateTimeTool.parseDateTime(new Date(), "yyyyMMdd");
            log.info("Date folder: {}", dateFolder);
            
            String fullFolderPath = UPLOAD_FOLDER + dateFolder + File.separator;
            File folder = new File(fullFolderPath);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                log.info("Folder created: {}, path: {}", created, fullFolderPath);
            }

            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            String fullFilePath = fullFolderPath + uniqueFileName;
            log.info("Saving file to: {}", fullFilePath);
            
            Path destPath = Paths.get(fullFilePath);
            Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
            
            String returnUrl = "/uploads/" + dateFolder + "/" + uniqueFileName;
            log.info("File saved successfully, returning URL: {}", returnUrl);
            
            return returnUrl;
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return null;
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
