package com.phonepe.platform.forage.core.utility;

import com.phonepe.platform.forage.models.StoredData;

import java.util.function.Consumer;

public class ParallelBootstrappingListDataStore<D, S extends StoredData<D>> extends ListDataStore<D, S> {
    @Override
    public void bootstrap(final Consumer<S> itemConsumer) {
        database.parallelStream().forEach(itemConsumer);
    }
}
