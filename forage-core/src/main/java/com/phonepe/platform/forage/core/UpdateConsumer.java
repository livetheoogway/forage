package com.phonepe.platform.forage.core;


public interface UpdateConsumer<U> {
    void consume(U update);
}
