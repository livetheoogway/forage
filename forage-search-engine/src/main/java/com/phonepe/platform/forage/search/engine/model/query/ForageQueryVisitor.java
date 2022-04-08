package com.phonepe.platform.forage.search.engine.model.query;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;

public interface ForageQueryVisitor<T> {
    T visit(ForageSearchQuery forageSearchQuery) throws ForageSearchError;

    T visit(PageQuery pageQuery) throws ForageSearchError;
}
