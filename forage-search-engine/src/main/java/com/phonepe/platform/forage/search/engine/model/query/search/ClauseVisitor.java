package com.phonepe.platform.forage.search.engine.model.query.search;

public interface ClauseVisitor<T> {
    T must();

    T should();

    T mustNot();

    T filter();

}
