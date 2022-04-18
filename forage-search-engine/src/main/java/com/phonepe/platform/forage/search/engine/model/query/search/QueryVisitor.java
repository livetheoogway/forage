package com.phonepe.platform.forage.search.engine.model.query.search;

@SuppressWarnings("java:S112")
public interface QueryVisitor<T> {
    T visit(BooleanQuery booleanQuery) throws Exception;

    T visit(MatchQuery matchQuery) throws Exception;

    T visit(ParsableQuery parsableQuery) throws Exception;

    T visit(RangeQuery rangeQuery);
}
