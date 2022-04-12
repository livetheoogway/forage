package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.core.model.TestDataItem;
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
        SimpleListDataStore simpleListDataStore = new SimpleListDataStore();
        collectingListener = new CollectingItemConsumer<>();
        periodicUpdateEngine = new PeriodicUpdateEngine<>(simpleListDataStore,
                                                          new AsyncQueuedConsumer<>(collectingListener),
                                                          1,
                                                          TimeUnit.SECONDS);
        for (int i = 0; i < 1000; i++) {
            simpleListDataStore.addData(new TestDataItem(String.valueOf(i), "This is message: " + i));
        }
    }

    @Test
    void test() {
        periodicUpdateEngine.start();
        Awaitility.await().atMost(Duration.of(500, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> collectingListener.size() >= 1000);
        periodicUpdateEngine.stop();
        Assertions.assertEquals(1000, collectingListener.size());
    }
}