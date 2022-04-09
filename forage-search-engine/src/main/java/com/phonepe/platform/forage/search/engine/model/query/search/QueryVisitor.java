package com.phonepe.platform.forage.search.engine.model.query.search;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;

public interface QueryVisitor<T> {
    T visit(BooleanQuery booleanQuery) throws ForageSearchError;

    T visit(MatchQuery matchQuery) throws ForageSearchError;

    T visit(ParsableQuery parsableQuery) throws ForageSearchError;

    T visit(RangeQuery rangeQuery);
}
