package com.phonepe.platform.forage.core;

import com.google.common.collect.Lists;

import java.util.List;

public class CollectingUpdateListener<T> implements UpdateListener<T> {
    private final List<T> collectedItems = Lists.newArrayList();

    @Override
    public void takeUpdate(final T dataItem) {
        collectedItems.add(dataItem);
    }

    public int size() {
        return collectedItems.size();
    }
}
