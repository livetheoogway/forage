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

import com.google.common.collect.Lists;

import java.util.List;

public class CollectingItemConsumer<T> implements ItemConsumer<T> {
    private final List<T> collectedItems = Lists.newArrayList();

    @Override
    public void init() {
        collectedItems.clear();
    }

    @Override
    public void consume(final T dataItem) {
        collectedItems.add(dataItem);
    }

    @Override
    public void finish() {

    }

    public int size() {
        return collectedItems.size();
    }
}
