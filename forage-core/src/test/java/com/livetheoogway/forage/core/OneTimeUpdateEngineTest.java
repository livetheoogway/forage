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

    private OneTimeUpdateEngine<String, TestDataItem> oneTimeUpdateEngine;
    private CollectingItemConsumer<TestDataItem> itemConsumer;
    private ListDataStore<String, TestDataItem> listDataStore;

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