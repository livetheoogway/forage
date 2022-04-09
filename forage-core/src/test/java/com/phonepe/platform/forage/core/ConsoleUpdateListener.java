package com.phonepe.platform.forage.core;

public class ConsoleUpdateListener<T> implements UpdateListener<T> {
    @Override
    public void takeUpdate(final T dataItem) {
        System.out.println("Received data item " + dataItem);
    }
}
