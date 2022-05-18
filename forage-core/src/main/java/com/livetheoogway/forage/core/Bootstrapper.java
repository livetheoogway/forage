package com.livetheoogway.forage.core;

import com.livetheoogway.forage.models.StoredData;

import java.util.function.Consumer;

/**
 * Bootstrap is the process of building something up from scratch.
 * This interface needs to be implemented by the datastore that is responsible for supplying items for bootstrap.
 * The interface is used by the {@link UpdateEngine#bootstrap()} method, to invoke the consumption of items from the
 * datastore (essentially the class that implements this interface)
 *
 * @param <D> Actual data item
 * @param <S> Stored data of type D that can be referenced with an id
 */
public interface Bootstrapper<D, S extends StoredData<D>> {
    /**
     * @param itemConsumer a consumer of items
     */
    void bootstrap(final Consumer<S> itemConsumer);
}
