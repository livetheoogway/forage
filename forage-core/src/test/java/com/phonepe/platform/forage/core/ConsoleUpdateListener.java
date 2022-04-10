package com.phonepe.platform.forage.core;

public class ConsoleUpdateListener<T> implements UpdateListener<T> {
    @Override
    public void init() throws Exception {

    }

    @Override
    public void takeUpdate(final T dataItem) throws Exception {
        System.out.println("Received data item " + dataItem);
    }

    @Override
    public void finish() throws Exception {

    }
}
