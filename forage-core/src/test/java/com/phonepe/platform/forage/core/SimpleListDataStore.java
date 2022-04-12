package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.core.model.TestDataItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleListDataStore implements Bootstrapper<String, TestDataItem> {

    private final List<TestDataItem> database = new ArrayList<>();

    @Override
    public void bootstrap(final Consumer<TestDataItem> itemConsumer) {
        database.parallelStream().forEach(itemConsumer);
    }

    public void addData(TestDataItem testDataItem) {
        database.add(testDataItem);
    }
}
