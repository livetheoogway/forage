package com.livetheoogway.forage.search.engine.model.index;

public interface DocumentVisitor<T> {
    T visit(ForageDocument forageDocument);

    T visit(LuceneDocument luceneDocument);

}
