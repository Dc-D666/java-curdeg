package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrendData {
    @SerializedName(value = "date", alternate = "time")
    private String date;
    
    @SerializedName(value = "count", alternate = "value")
    private Integer count;
    
    @SerializedName(value = "passed", alternate = "approved")
    private Integer passed;
    
    @SerializedName(value = "rejected", alternate = "denied")
    private Integer rejected;
    
    @SerializedName(value = "pending", alternate = "waiting")
    private Integer pending;

    public TrendData() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getPassed() {
        return passed;
    }

    public void setPassed(Integer passed) {
        this.passed = passed;
    }

    public Integer getRejected() {
        return rejected;
    }

    public void setRejected(Integer rejected) {
        this.rejected = rejected;
    }

    public Integer getPending() {
        return pending;
    }

    public void setPending(Integer pending) {
        this.pending = pending;
    }
}
