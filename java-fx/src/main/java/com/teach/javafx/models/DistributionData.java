package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

public class DistributionData {
    @SerializedName(value = "name", alternate = "label")
    private String name;
    
    @SerializedName(value = "count", alternate = "value")
    private Integer count;
    
    @SerializedName(value = "percentage", alternate = "rate")
    private Double percentage;
    
    @SerializedName(value = "color", alternate = "category")
    private String color;

    public DistributionData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
