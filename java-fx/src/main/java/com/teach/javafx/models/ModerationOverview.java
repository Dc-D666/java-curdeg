package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

public class ModerationOverview {
    @SerializedName(value = "totalModerationCount", alternate = "totalCount")
    private Integer totalModerationCount;
    
    @SerializedName(value = "pendingModerationCount", alternate = "pendingCount")
    private Integer pendingModerationCount;
    
    @SerializedName(value = "passedModerationCount", alternate = "passedCount")
    private Integer passedModerationCount;
    
    @SerializedName(value = "rejectedModerationCount", alternate = "rejectedCount")
    private Integer rejectedModerationCount;
    
    @SerializedName(value = "passRate", alternate = "passedPercentage")
    private Double passRate;
    
    @SerializedName(value = "rejectRate", alternate = "rejectedPercentage")
    private Double rejectRate;
    
    @SerializedName(value = "manualModerationCount", alternate = "manualCount")
    private Integer manualModerationCount;
    
    @SerializedName(value = "manualModerationRate", alternate = "manualPercentage")
    private Double manualModerationRate;

    public ModerationOverview() {
    }

    public Integer getTotalModerationCount() {
        return totalModerationCount;
    }

    public void setTotalModerationCount(Integer totalModerationCount) {
        this.totalModerationCount = totalModerationCount;
    }

    public Integer getPendingModerationCount() {
        return pendingModerationCount;
    }

    public void setPendingModerationCount(Integer pendingModerationCount) {
        this.pendingModerationCount = pendingModerationCount;
    }

    public Integer getPassedModerationCount() {
        return passedModerationCount;
    }

    public void setPassedModerationCount(Integer passedModerationCount) {
        this.passedModerationCount = passedModerationCount;
    }

    public Integer getRejectedModerationCount() {
        return rejectedModerationCount;
    }

    public void setRejectedModerationCount(Integer rejectedModerationCount) {
        this.rejectedModerationCount = rejectedModerationCount;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public Double getRejectRate() {
        return rejectRate;
    }

    public void setRejectRate(Double rejectRate) {
        this.rejectRate = rejectRate;
    }

    public Integer getManualModerationCount() {
        return manualModerationCount;
    }

    public void setManualModerationCount(Integer manualModerationCount) {
        this.manualModerationCount = manualModerationCount;
    }

    public Double getManualModerationRate() {
        return manualModerationRate;
    }

    public void setManualModerationRate(Double manualModerationRate) {
        this.manualModerationRate = manualModerationRate;
    }
}
