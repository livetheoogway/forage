package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.core.model.TestDataItem;
import com.phonepe.platform.forage.core.utility.ListDataStore;
import com.phonepe.platform.forage.core.utility.ParallelBootstrappingListDataStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

class AsyncQueuedConsumerTest {

    private UpdateEngine<String, TestDataItem> updateEngine;
    private CollectingItemConsumer<TestDataItem> itemConsumer;
    private ListDataStore<String, TestDataItem> listDataStore;

    @BeforeEach
    void setUp() {
        listDataStore = new ParallelBootstrappingListDataStore<>();
        itemConsumer = new CollectingItemConsumer<>();
        updateEngine = new PeriodicUpdateEngine<>(listDataStore,
                                                  new AsyncQueuedConsumer<>(itemConsumer),
                                                  1, TimeUnit.SECONDS);
    }

    @Test
    void testOneTimeUpdate() throws Exception {
        for (int i = 0; i < 100; i++) {
            listDataStore.addData(new TestDataItem(String.valueOf(i), "This is message: " + i));
        }
        updateEngine.start();
        Assertions.assertTrue(100 > itemConsumer.size());

        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> itemConsumer.size() == 100);

        /* adding 100 more items */
        for (int i = 0; i < 100; i++) {
            listDataStore.addData(new TestDataItem(String.valueOf(i), "This is message: " + i));
        }

        /* the one time update should not have consumed the newer data items added */
        Assertions.assertEquals(100, itemConsumer.size());

        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> itemConsumer.size() == 200);

        updateEngine.stop();
    }
}