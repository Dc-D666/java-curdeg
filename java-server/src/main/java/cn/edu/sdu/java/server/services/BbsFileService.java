package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.util.DateTimeTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class BbsFileService {
    private static final Logger log = LoggerFactory.getLogger(BbsFileService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Set<String> BLOCKED_ATTACHMENT_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "msi", "dll", "scr", "com", "jar", "class", "sh", "ps1", "vbs", "reg"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    public static final long MAX_ATTACHMENT_SIZE = 20 * 1024 * 1024;
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

    public Map<String, Object> uploadAttachment(MultipartFile file) {
        log.info("Starting attachment upload, originalFilename: {}, size: {}", file.getOriginalFilename(), file.getSize());

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("附件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("附件文件名不能为空");
            }

            String safeOriginalName = Paths.get(originalFilename).getFileName().toString();
            String extension = getFileExtension(safeOriginalName).toLowerCase();
            if (extension.isBlank()) {
                throw new IllegalArgumentException("附件必须包含文件扩展名");
            }
            if (BLOCKED_ATTACHMENT_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("不支持上传可执行或高风险附件");
            }
            if (file.getSize() > MAX_ATTACHMENT_SIZE) {
                throw new IllegalArgumentException("单个附件不能超过20MB");
            }

            String dateFolder = DateTimeTool.parseDateTime(new Date(), "yyyyMMdd");
            String fullFolderPath = UPLOAD_FOLDER + dateFolder + File.separator;
            File folder = new File(fullFolderPath);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                log.info("Folder created: {}, path: {}", created, fullFolderPath);
            }

            String uniqueFileName = UUID.randomUUID() + "." + extension;
            Path destPath = Paths.get(fullFolderPath + uniqueFileName);
            Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> result = new HashMap<>();
            result.put("url", "/uploads/" + dateFolder + "/" + uniqueFileName);
            result.put("name", safeOriginalName);
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            result.put("ext", extension);
            return result;
        } catch (IOException e) {
            log.error("Failed to upload attachment", e);
            throw new IllegalStateException("附件上传失败，请稍后重试");
        }
    }

    public String normalizeAttachmentInfos(String attachmentInfos) {
        if (attachmentInfos == null || attachmentInfos.isBlank() || "[]".equals(attachmentInfos.trim())) {
            return null;
        }

        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                    attachmentInfos, new TypeReference<List<Map<String, Object>>>() {}
            );
            if (items.size() > 5) {
                throw new IllegalArgumentException("附件最多上传5个");
            }

            for (Map<String, Object> item : items) {
                String url = valueAsString(item.get("url"));
                String name = valueAsString(item.get("name"));
                String ext = valueAsString(item.get("ext")).toLowerCase();
                long size = valueAsLong(item.get("size"));

                if (url.isBlank() || !url.startsWith("/uploads/")) {
                    throw new IllegalArgumentException("附件地址不合法");
                }
                if (name.isBlank() || name.length() > 255) {
                    throw new IllegalArgumentException("附件名称不合法");
                }
                if (ext.isBlank() || BLOCKED_ATTACHMENT_EXTENSIONS.contains(ext)) {
                    throw new IllegalArgumentException("附件类型不合法");
                }
                if (size <= 0 || size > MAX_ATTACHMENT_SIZE) {
                    throw new IllegalArgumentException("单个附件不能超过20MB");
                }
            }

            return objectMapper.writeValueAsString(items);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("附件信息格式不正确");
        }
    }

    private String valueAsString(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private long valueAsLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(valueAsString(value));
        } catch (Exception e) {
            return -1L;
        }
    }
    
    public String downloadAndSaveImage(String imageUrl) {
        log.info("Starting to download and save image from URL: {}", imageUrl);
        
        try {
            URL url = URI.create(imageUrl).toURL();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            try (InputStream inputStream = connection.getInputStream()) {
                String dateFolder = DateTimeTool.parseDateTime(new Date(), "yyyyMMdd");
                log.info("Date folder for downloaded image: {}", dateFolder);
                
                String fullFolderPath = UPLOAD_FOLDER + dateFolder + File.separator;
                File folder = new File(fullFolderPath);
                if (!folder.exists()) {
                    boolean created = folder.mkdirs();
                    log.info("Folder created: {}, path: {}", created, fullFolderPath);
                }
                
                String uniqueFileName = UUID.randomUUID().toString() + ".png";
                String fullFilePath = fullFolderPath + uniqueFileName;
                log.info("Saving downloaded image to: {}", fullFilePath);
                
                Path destPath = Paths.get(fullFilePath);
                Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
                
                String returnUrl = "/uploads/" + dateFolder + "/" + uniqueFileName;
                log.info("Downloaded image saved successfully, returning URL: {}", returnUrl);
                
                return returnUrl;
            }
        } catch (IOException e) {
            log.error("Failed to download and save image from URL: {}", imageUrl, e);
            return null;
        }
    }
}
