package com.livetheoogway.forage.search.engine.model;

import com.livetheoogway.forage.models.StoredData;

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
