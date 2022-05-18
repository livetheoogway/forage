package com.livetheoogway.forage.core;

public class ConsolePrintConsumer<T> implements ItemConsumer<T> {
    @Override
    public void init() throws Exception {

    }

    @Override
    public void consume(final T dataItem) {
        System.out.println("Received data item " + dataItem);
    }

    @Override
    public void finish() {

    }
}
