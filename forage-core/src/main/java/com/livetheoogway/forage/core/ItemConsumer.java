/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

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
