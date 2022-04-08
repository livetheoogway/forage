package com.phonepe.platform.forage.search.engine.model;

public interface DocumentVisitor<T> {
    T visit(ForageDocument forageDocument);

    T visit(LuceneDocument luceneDocument);
}
