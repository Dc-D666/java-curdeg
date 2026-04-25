package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

public class ReportOverview {
    @SerializedName(value = "totalReportCount", alternate = "totalCount")
    private Integer totalReportCount;
    
    @SerializedName(value = "pendingReportCount", alternate = "pendingCount")
    private Integer pendingReportCount;
    
    @SerializedName(value = "processedReportCount", alternate = "processedCount")
    private Integer processedReportCount;
    
    @SerializedName(value = "processRate", alternate = "processedPercentage")
    private Double processRate;

    public ReportOverview() {
    }

    public Integer getTotalReportCount() {
        return totalReportCount;
    }

    public void setTotalReportCount(Integer totalReportCount) {
        this.totalReportCount = totalReportCount;
    }

    public Integer getPendingReportCount() {
        return pendingReportCount;
    }

    public void setPendingReportCount(Integer pendingReportCount) {
        this.pendingReportCount = pendingReportCount;
    }

    public Integer getProcessedReportCount() {
        return processedReportCount;
    }

    public void setProcessedReportCount(Integer processedReportCount) {
        this.processedReportCount = processedReportCount;
    }

    public Double getProcessRate() {
        return processRate;
    }

    public void setProcessRate(Double processRate) {
        this.processRate = processRate;
    }
}
