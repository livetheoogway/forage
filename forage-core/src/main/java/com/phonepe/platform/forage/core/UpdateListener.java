package com.phonepe.platform.forage.core;

public interface UpdateListener<U> {
    void init() throws Exception;

    void takeUpdate(U u) throws Exception;

    void finish() throws Exception;
}
