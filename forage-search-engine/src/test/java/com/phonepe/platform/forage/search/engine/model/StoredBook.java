package com.phonepe.platform.forage.search.engine.model;

import com.phonepe.platform.forage.models.StoredData;

public class StoredBook implements StoredData<Book> {
    private Book book;
    private String id;

    @Override
    public String id() {
        return id;
    }

    @Override
    public Book data() {
        return book;
    }
}
