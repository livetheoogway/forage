package com.phonepe.platform.forage.models;

public interface StoredData<T> {
    String id();
    T data();
}
