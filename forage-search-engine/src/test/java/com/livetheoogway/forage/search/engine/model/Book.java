package com.livetheoogway.forage.search.engine.model;

import com.google.common.collect.ImmutableList;
import com.livetheoogway.forage.models.result.field.Field;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.TextField;
import lombok.Value;

import java.util.List;

@Value
public class Book {
    String id;
    String title;
    String author;
    float rating;
    String language;
    int numPage;

    public List<Field> fields() {
        return ImmutableList.of(new TextField("title", title),
                                new TextField("author", author),
                                new FloatField("rating", new float[]{rating}),
                                new IntField("numPage", new int[]{numPage}));
    }
}