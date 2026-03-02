package com.example.autowebgenerator.common;

import java.io.Serial;
import java.io.Serializable;

public class PaginationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int page = 1;
    private int size = 10;
    private String sortBy;
    private String sortOrder = "desc";

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
