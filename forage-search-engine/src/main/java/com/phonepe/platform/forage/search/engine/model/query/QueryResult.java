package com.phonepe.platform.forage.search.engine.model.query;

import lombok.Value;

import java.util.List;

@Value
public class QueryResult<D> {
    List<D> documents;
    String nextPage;
}
