package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PageResult<T> {
    @SerializedName(value = "total", alternate = "totalElements")
    private Long total;
    @SerializedName(value = "list", alternate = "content")
    private List<T> list;
    @SerializedName(value = "pageNum", alternate = {"pageNumber", "number"})
    private Integer pageNum;
    @SerializedName(value = "pageSize", alternate = "size")
    private Integer pageSize;

    public PageResult() {
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
