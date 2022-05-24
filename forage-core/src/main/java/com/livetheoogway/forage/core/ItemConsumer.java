package com.livetheoogway.forage.core;

/**
 * A consumer of updates, which starts with the init process and then is finally finished
 *
 * @param <I> type of item
 */
@SuppressWarnings("java:S112")
public interface ItemConsumer<I> {

    /**
     * make the consumer ready for consuming items here
     *
     * @throws Exception if any during init
     */
    void init() throws Exception;

    /**
     * consumer the item
     *
     * @param item item to be consumed
     * @throws Exception if any during consumption of item
     */
    void consume(I item) throws Exception;

    /**
     * this finish shall be called once all the items are consumed
     *
     * @throws Exception if any during finish
     */
    void finish() throws Exception;
}
