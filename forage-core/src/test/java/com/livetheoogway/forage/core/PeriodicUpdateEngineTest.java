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

import com.livetheoogway.forage.core.model.TestDataItem;
import com.livetheoogway.forage.core.utility.ListDataStore;
import com.livetheoogway.forage.core.utility.ParallelBootstrappingListDataStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

class PeriodicUpdateEngineTest {

    private PeriodicUpdateEngine<TestDataItem> periodicUpdateEngine;
    private CollectingItemConsumer<TestDataItem> collectingListener;

    @BeforeEach
    void setUp() {
        ListDataStore<TestDataItem> parallelBootstrappingListDataStore =
                new ParallelBootstrappingListDataStore<>();
        collectingListener = new CollectingItemConsumer<>();
        periodicUpdateEngine = new PeriodicUpdateEngine<>(parallelBootstrappingListDataStore,
                                                          new AsyncQueuedConsumer<>(collectingListener),
                                                          1,
                                                          TimeUnit.SECONDS);
        for (int i = 0; i < 1000; i++) {
            parallelBootstrappingListDataStore.addData(new TestDataItem(String.valueOf(i), "This is message: " + i));
        }
    }

    @Test
    void test() {
        periodicUpdateEngine.start();
        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> collectingListener.size() >= 1000);
        periodicUpdateEngine.stop();
        Assertions.assertEquals(1000, collectingListener.size());
    }
}