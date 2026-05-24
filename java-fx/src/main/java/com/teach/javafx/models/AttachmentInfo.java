package com.teach.javafx.models;

public class AttachmentInfo {
    private String url;
    private String name;
    private Long size;
    private String contentType;
    private String ext;

    public AttachmentInfo() {
    }

    public AttachmentInfo(String url, String name, Long size, String contentType, String ext) {
        this.url = url;
        this.name = name;
        this.size = size;
        this.contentType = contentType;
        this.ext = ext;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}
