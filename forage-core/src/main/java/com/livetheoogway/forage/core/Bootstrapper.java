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
