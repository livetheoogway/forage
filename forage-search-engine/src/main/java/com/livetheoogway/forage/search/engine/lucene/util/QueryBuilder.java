package com.livetheoogway.forage.search.engine.lucene.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryBuilder {

    public Builders.InnerBooleanQueryBuilder booleanQuery() {
        return new Builders.InnerBooleanQueryBuilder();
    }

    public Builders.InnerMatchQueryBuilder matchQuery(String field, String value) {
        return new Builders.InnerMatchQueryBuilder(field, value);
    }

    public Builders.InnerFuzzyMatchQueryBuilder fuzzyMatchQuery(String field, String value) {
        return new Builders.InnerFuzzyMatchQueryBuilder(field, value);
    }

    public Builders.InnerIntRangeQueryBuilder intRangeQuery(String field, int low, int high) {
        return new Builders.InnerIntRangeQueryBuilder(field, low, high);
    }

    public Builders.InnerFloatRangeQueryBuilder floatRangeQuery(String field, float low, float high) {
        return new Builders.InnerFloatRangeQueryBuilder(field, low, high);
    }
}
