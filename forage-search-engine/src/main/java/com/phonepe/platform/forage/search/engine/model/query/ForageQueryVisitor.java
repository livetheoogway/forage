package com.phonepe.platform.forage.search.engine.model.query;

@SuppressWarnings("java:S112")
public interface ForageQueryVisitor<T> {
    T visit(ForageSearchQuery forageSearchQuery) throws Exception;

    T visit(PageQuery pageQuery) throws Exception;
}
