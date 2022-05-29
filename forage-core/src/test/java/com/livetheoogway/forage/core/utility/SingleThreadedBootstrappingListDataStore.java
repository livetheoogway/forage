package com.livetheoogway.forage.core.utility;

import com.livetheoogway.forage.models.DataId;

import java.util.function.Consumer;

public class SingleThreadedBootstrappingListDataStore<D extends DataId> extends ListDataStore<D> {
    @Override
    public void bootstrap(final Consumer<D> itemConsumer) {
        database.forEach(itemConsumer);
    }
}
