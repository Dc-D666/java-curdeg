package cn.edu.sdu.java.server.util;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AttachmentStorageUtil {
    public static final String UPLOAD_URL_PREFIX = "/uploads/";

    private AttachmentStorageUtil() {
    }

    public static Path resolvePrimaryUploadRoot(String attachFolder) {
        Path configuredPath = Paths.get(defaultAttachFolder(attachFolder));
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize().toAbsolutePath();
        }

        Path currentDir = currentWorkingDirectory();
        Path projectDir = resolveProjectDir(currentDir);
        return projectDir.resolve(normalizeAttachFolder(attachFolder)).normalize();
    }

    public static List<Path> resolveCandidateUploadRoots(String attachFolder) {
        Path configuredPath = Paths.get(defaultAttachFolder(attachFolder));
        Set<Path> roots = new LinkedHashSet<>();
        if (configuredPath.isAbsolute()) {
            roots.add(configuredPath.normalize().toAbsolutePath());
            return new ArrayList<>(roots);
        }

        String normalizedFolder = normalizeAttachFolder(attachFolder);
        Path currentDir = currentWorkingDirectory();
        Path projectDir = resolveProjectDir(currentDir);
        roots.add(projectDir.resolve(normalizedFolder).normalize());
        roots.add(currentDir.resolve(normalizedFolder).normalize());
        Path parentDir = currentDir.getParent();
        if (parentDir != null) {
            roots.add(parentDir.resolve(normalizedFolder).normalize());
        }
        return new ArrayList<>(roots);
    }

    public static Path resolveExistingUpload(String attachFolder, String uploadUrl) {
        String relativePath = extractRelativeUploadPath(uploadUrl);
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }

        for (Path root : resolveCandidateUploadRoots(attachFolder)) {
            Path candidate = root.resolve(relativePath).normalize();
            if (!candidate.startsWith(root)) {
                continue;
            }
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static String toResourceLocation(Path directory) {
        String location = directory.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    private static String extractRelativeUploadPath(String uploadUrl) {
        if (uploadUrl == null || uploadUrl.isBlank()) {
            return null;
        }

        String normalized = uploadUrl.trim().replace('\\', '/');
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            normalized = URI.create(normalized).getPath();
        }
        if (!normalized.startsWith(UPLOAD_URL_PREFIX)) {
            return null;
        }

        String relativePath = normalized.substring(UPLOAD_URL_PREFIX.length());
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return relativePath;
    }

    private static Path currentWorkingDirectory() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    private static Path resolveProjectDir(Path currentDir) {
        Path cursor = currentDir;
        while (cursor != null) {
            if (looksLikeJavaServerModule(cursor)) {
                return cursor.toAbsolutePath().normalize();
            }
            cursor = cursor.getParent();
        }

        Path moduleDir = currentDir.resolve("java-server");
        if (looksLikeJavaServerModule(moduleDir)) {
            return moduleDir.toAbsolutePath().normalize();
        }

        return currentDir.toAbsolutePath().normalize();
    }

    private static String normalizeAttachFolder(String attachFolder) {
        String folder = defaultAttachFolder(attachFolder).trim().replace('\\', '/');
        while (folder.startsWith("./")) {
            folder = folder.substring(2);
        }
        while (folder.startsWith("/")) {
            folder = folder.substring(1);
        }
        while (folder.endsWith("/")) {
            folder = folder.substring(0, folder.length() - 1);
        }
        return folder.isBlank() ? "uploads" : folder;
    }

    private static String defaultAttachFolder(String attachFolder) {
        return attachFolder == null || attachFolder.isBlank() ? "./uploads/" : attachFolder;
    }

    private static boolean looksLikeJavaServerModule(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return false;
        }
        return Files.exists(directory.resolve("pom.xml"))
                && Files.isDirectory(directory.resolve("src").resolve("main"));
    }
}
