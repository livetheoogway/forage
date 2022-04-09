package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.core.model.DataItem;

import java.util.ArrayList;
import java.util.List;

public class DataStore implements Bootstrapper<String, DataItem> {

    private final List<DataItem> database = new ArrayList<>();

    @Override
    public void bootstrap(final UpdateConsumer<DataItem> updateConsumer) {
        updateConsumer.init();
        database.parallelStream().forEach(updateConsumer::consume);
        updateConsumer.finish();
    }

    public void addData(DataItem dataItem) {
        database.add(dataItem);
    }
}
