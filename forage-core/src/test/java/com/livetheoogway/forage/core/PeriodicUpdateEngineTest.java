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

    private PeriodicUpdateEngine<String, TestDataItem> periodicUpdateEngine;
    private CollectingItemConsumer<TestDataItem> collectingListener;

    @BeforeEach
    void setUp() {
        ListDataStore<String, TestDataItem> parallelBootstrappingListDataStore =
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