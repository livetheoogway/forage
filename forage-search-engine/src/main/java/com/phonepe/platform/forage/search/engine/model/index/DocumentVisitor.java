package com.phonepe.platform.forage.search.engine.model.index;

public interface DocumentVisitor<T, D> {
    T visit(ForageDocument<D> forageDocument);

    T visit(LuceneDocument<D> luceneDocument);

}
