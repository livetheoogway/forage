package com.livetheoogway.forage.core;

import com.livetheoogway.forage.models.DataId;

import java.util.function.Consumer;

/**
 * Bootstrap is the process of building something up from scratch.
 * This interface needs to be implemented by the datastore that is responsible for supplying items for bootstrap.
 * The interface is used by the {@link UpdateEngine#bootstrap()} method, to invoke the consumption of items from the
 * datastore (essentially the class that implements this interface)
 *
 * @param <D> Any data item that has an id
 */
public interface Bootstrapper<D extends DataId> {
    /**
     * @param itemConsumer a consumer of items
     */
    void bootstrap(final Consumer<D> itemConsumer);
}
