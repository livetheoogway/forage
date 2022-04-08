package com.phonepe.platform.forage.search.engine.model.store;

public interface Storable<T> {
    String id();
    T data();
}
