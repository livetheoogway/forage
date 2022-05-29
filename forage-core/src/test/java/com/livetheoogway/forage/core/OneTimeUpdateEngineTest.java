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
import com.livetheoogway.forage.core.utility.SingleThreadedBootstrappingListDataStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class OneTimeUpdateEngineTest {

    private OneTimeUpdateEngine<TestDataItem> oneTimeUpdateEngine;
    private CollectingItemConsumer<TestDataItem> itemConsumer;
    private ListDataStore<TestDataItem> listDataStore;

    @BeforeEach
    void setUp() {
        listDataStore = new SingleThreadedBootstrappingListDataStore<>();
        itemConsumer = new CollectingItemConsumer<>();
        oneTimeUpdateEngine = new OneTimeUpdateEngine<>(listDataStore,
                                                        new AsyncQueuedConsumer<>(itemConsumer));
    }

    @Test
    void testOneTimeUpdate() throws Exception {
        for (int i = 0; i < 100; i++) {
            listDataStore.addData(new TestDataItem(String.valueOf(i), "This is message: " + i));
        }
        oneTimeUpdateEngine.start();
        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> itemConsumer.size() == 100);
        Assertions.assertEquals(100, itemConsumer.size());

        /* adding 100 more items */
        for (int i = 0; i < 100; i++) {
            listDataStore.addData(new TestDataItem(String.valueOf(i), "This is message: " + i));
        }

        /* the one time update should not have consumed the newer data items added */
        Assertions.assertEquals(100, itemConsumer.size());
        oneTimeUpdateEngine.stop();
    }
}