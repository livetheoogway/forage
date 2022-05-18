package com.livetheoogway.forage.models;

public interface StoredData<T> {
    String id();
    T data();
}
