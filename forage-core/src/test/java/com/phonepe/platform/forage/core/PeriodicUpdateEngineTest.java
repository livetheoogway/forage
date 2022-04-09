package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.core.model.DataItem;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

class PeriodicUpdateEngineTest {

    private PeriodicUpdateEngine<String, DataItem> periodicUpdateEngine;
    private CollectingUpdateListener<DataItem> collectingListener;

    @BeforeEach
    void setUp() {
        DataStore dataStore = new DataStore();
        collectingListener = new CollectingUpdateListener<>();
        periodicUpdateEngine = new PeriodicUpdateEngine<>(dataStore,
                                                          new AsyncQueuedConsumer<>(collectingListener), 1,
                                                          TimeUnit.SECONDS);
        for (int i = 0; i < 1000; i++) {
            dataStore.addData(new DataItem(String.valueOf(i), "This is message: " + i));
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