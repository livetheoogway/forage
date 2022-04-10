package com.phonepe.platform.forage.core;

import com.google.common.collect.Lists;

import java.util.List;

public class CollectingUpdateListener<T> implements UpdateListener<T> {
    private final List<T> collectedItems = Lists.newArrayList();

    @Override
    public void init() throws Exception {
        collectedItems.clear();
    }

    @Override
    public void takeUpdate(final T dataItem) throws Exception {
        collectedItems.add(dataItem);
    }

    @Override
    public void finish() throws Exception {
        collectedItems.clear();
    }

    public int size() {
        return collectedItems.size();
    }
}
