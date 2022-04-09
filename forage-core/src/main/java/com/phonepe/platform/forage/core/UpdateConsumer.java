package com.phonepe.platform.forage.core;


public interface UpdateConsumer<U> {
    void init();
    void consume(U update);
    void finish();
}
