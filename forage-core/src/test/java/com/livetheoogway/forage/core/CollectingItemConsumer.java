package com.livetheoogway.forage.core;

import com.google.common.collect.Lists;

import java.util.List;

public class CollectingItemConsumer<T> implements ItemConsumer<T> {
    private final List<T> collectedItems = Lists.newArrayList();

    @Override
    public void init() {
        collectedItems.clear();
    }

    @Override
    public void consume(final T dataItem) {
        collectedItems.add(dataItem);
    }

    @Override
    public void finish() {

    }

    public int size() {
        return collectedItems.size();
    }
}
