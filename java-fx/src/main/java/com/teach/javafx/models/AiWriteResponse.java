package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

public class AiWriteResponse {
    private String title;
    private String content;
    private String instructionSuggestion;
    private Boolean success;
    private String message;

    public AiWriteResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInstructionSuggestion() {
        return instructionSuggestion;
    }

    public void setInstructionSuggestion(String instructionSuggestion) {
        this.instructionSuggestion = instructionSuggestion;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
