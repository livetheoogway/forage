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

package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.core.AsyncQueuedConsumer;
import com.livetheoogway.forage.core.PeriodicUpdateEngine;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.range.IntRange;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.TestUtils;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.Book;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.BookDataStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class PeriodicallyUpdatedForageSearchEngineTest {

    @Test
    void testPeriodicallyUpdatedQueryEngine() throws Exception {
        final BookDataStore dataStore = new BookDataStore();
        final ForageEngineIndexer<Book> luceneQueryEngineContainer = new ForageEngineIndexer<>(
                ForageSearchEngineBuilder.<Book>builder()
                        .withDataStore(dataStore)
                        .withObjectMapper(TestUtils.mapper()));

        dataStore.addBooks(1);

        final PeriodicUpdateEngine<IndexableDocument> periodicUpdateEngine =
                new PeriodicUpdateEngine<>(
                        dataStore, new AsyncQueuedConsumer<>(luceneQueryEngineContainer),
                        1, TimeUnit.SECONDS
                );
        periodicUpdateEngine.bootstrap();
        Awaitility.await().atMost(Duration.of(50, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .ignoreExceptionsMatching(throwable -> throwable instanceof ForageSearchError
                        && ((ForageSearchError) throwable).getForageErrorCode()
                        == ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET)
                .until(() -> {
                    final ForageQueryResult<Book> query = luceneQueryEngineContainer.search(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query.getTotal().getTotal() == 1;
                });

        periodicUpdateEngine.start();

        dataStore.addBooks(4);

        /* increment the datastore by 5 books */
        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> {
                    final ForageQueryResult<Book> query = luceneQueryEngineContainer.search(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query.getTotal().getTotal() == 5;
                });

        final ForageQueryResult<Book> query = luceneQueryEngineContainer.search(
                new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
        Assertions.assertEquals(5, query.getTotal().getTotal());

        /* increment the datastore by 2 more books */
        dataStore.addBooks(2);
        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> {
                    final ForageQueryResult<Book> query2 = luceneQueryEngineContainer.search(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query2.getTotal().getTotal() == 7;
                });

        final ForageQueryResult<Book> query2 = luceneQueryEngineContainer.search(
                new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
        Assertions.assertEquals(7, query2.getTotal().getTotal());

        periodicUpdateEngine.stop();
    }

    @Test
    void testPeriodicallyUpdatedQueryEngineWithFrequentQueries() throws Exception {
        final BookDataStore dataStore = new BookDataStore();
        final ForageEngineIndexer<Book> luceneQueryEngineContainer = new ForageEngineIndexer<>(
                ForageSearchEngineBuilder.<Book>builder()
                        .withDataStore(dataStore)
                        .withObjectMapper(TestUtils.mapper()));

        performParallelSearchExecutions(dataStore, luceneQueryEngineContainer);
    }

    private void performParallelSearchExecutions(final BookDataStore dataStore, final ForageEngineIndexer<Book> luceneQueryEngineContainer)
            throws Exception {
        dataStore.addBooks(1);

        final PeriodicUpdateEngine<IndexableDocument> periodicUpdateEngine =
                new PeriodicUpdateEngine<>(
                        dataStore, new AsyncQueuedConsumer<>(luceneQueryEngineContainer),
                        1, TimeUnit.SECONDS
                );
        periodicUpdateEngine.bootstrap();

        Awaitility.await().atMost(Duration.of(50, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .ignoreExceptionsMatching(throwable -> throwable instanceof ForageSearchError
                        && ((ForageSearchError) throwable).getForageErrorCode()
                        == ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET)
                .until(() -> {
                    final ForageQueryResult<Book> query = luceneQueryEngineContainer.search(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query.getTotal().getTotal() == 1;
                });

        periodicUpdateEngine.start();

        dataStore.addAllBooks();

        /* increment the datastore by 5 books */
        Awaitility.await().atMost(Duration.of(10, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> {
                    final ForageQueryResult<Book> query = luceneQueryEngineContainer.search(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query.getTotal().getTotal() >= 5;
                });


        final long time = System.currentTimeMillis();
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1000000; i++) {
            executorService.submit(() -> {
                final ForageQueryResult<Book> query;
                try {
                    query = luceneQueryEngineContainer.search(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                } catch (ForageSearchError e) {
                    throw new RuntimeException(e);
                }
                Assertions.assertEquals(1001, query.getTotal().getTotal());
            });
        }
        periodicUpdateEngine.stop();

        System.out.println("time = " + (System.currentTimeMillis() - time) + " " + Thread.currentThread()
                .getStackTrace()[2].getMethodName());
    }
}