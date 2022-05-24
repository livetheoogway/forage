package com.livetheoogway.forage.core;

import com.livetheoogway.forage.models.StoredData;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the main update engine that is responsible for exposing the boostrap function
 *
 * @param <D> Actual data item
 * @param <S> Stored data of type D that can be referenced with an id
 */
@SuppressWarnings("java:S112")
@Slf4j
public abstract class UpdateEngine<D, S extends StoredData<D>> {
    private final Bootstrapper<D, S> bootstrapper;
    private final ItemConsumer<S> itemConsumer;
    private final ErrorHandler<S> errorHandler;

    protected UpdateEngine(final Bootstrapper<D, S> bootstrapper,
                           final ItemConsumer<S> itemConsumer,
                           final ErrorHandler<S> errorHandler) {
        this.bootstrapper = bootstrapper;
        this.itemConsumer = itemConsumer;
        this.errorHandler = errorHandler;
    }

    /**
     * the primary function that is supposed to bootstrap all items into the consumer
     */
    public void bootstrap() throws Exception {
        itemConsumer.init();
        bootstrapper.bootstrap(item -> {
            try {
                itemConsumer.consume(item);
            } catch (Exception e) {
                errorHandler.handleError(item, e);
            }
        });
        itemConsumer.finish();
    }

    /**
     * start and operations of boostrap (eg: schedule any thread to bootstrap at regular intervals)
     *
     * @throws Exception if there was an issue during start-up
     */
    public abstract void start() throws Exception;

    /**
     * stop resources if any
     *
     * @throws Exception if there was an issue during stop
     */
    public abstract void stop() throws Exception;
}
