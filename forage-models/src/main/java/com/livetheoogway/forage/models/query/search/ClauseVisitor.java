package com.livetheoogway.forage.models.query.search;

public interface ClauseVisitor<T> {
    T must();

    T should();

    T mustNot();

    T filter();

}
