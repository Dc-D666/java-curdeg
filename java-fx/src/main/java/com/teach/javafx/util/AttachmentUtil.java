package com.teach.javafx.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teach.javafx.models.AttachmentInfo;
import com.teach.javafx.request.HttpRequestUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AttachmentUtil {
    private static final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<AttachmentInfo>>() {}.getType();

    private AttachmentUtil() {
    }

    public static List<AttachmentInfo> parse(String attachmentInfos) {
        if (attachmentInfos == null || attachmentInfos.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<AttachmentInfo> list = gson.fromJson(attachmentInfos, LIST_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static String toJson(List<AttachmentInfo> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        return gson.toJson(attachments);
    }

    public static String formatSize(Long size) {
        if (size == null || size < 0) {
            return "";
        }
        double value = size;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value = value / 1024.0;
            unitIndex++;
        }
        if (unitIndex == 0) {
            return ((long) value) + units[unitIndex];
        }
        return String.format("%.1f%s", value, units[unitIndex]);
    }

    public static String fullUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (url.startsWith("/")) {
            return HttpRequestUtil.serverUrl + url;
        }
        return HttpRequestUtil.serverUrl + "/" + url;
    }
}
