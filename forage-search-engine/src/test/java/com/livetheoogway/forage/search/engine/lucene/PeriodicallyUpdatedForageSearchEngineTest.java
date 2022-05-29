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
import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.core.PeriodicUpdateEngine;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.range.IntRange;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.ResourceReader;
import com.livetheoogway.forage.search.engine.TestUtils;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.Book;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.store.Store;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class PeriodicallyUpdatedForageSearchEngineTest {

    private static class DataStore implements Bootstrapper<IndexableDocument>, Store<Book> {
        private final AtomicInteger indexPosition;
        private final Map<String, Book> booksAvailableForIndexing;
        private final List<Book> fullGlossaryOfBook;

        public DataStore() throws IOException {
            this.fullGlossaryOfBook = ResourceReader.extractBooks();
            this.indexPosition = new AtomicInteger(0);
            this.booksAvailableForIndexing = new HashMap<>();
        }

        public void addBooks(int numberOfBooksToBeAddedForIndexing) {
            this.booksAvailableForIndexing.putAll(
                    fullGlossaryOfBook.subList(indexPosition.get(),
                                               indexPosition.get() + numberOfBooksToBeAddedForIndexing)
                            .stream().collect(Collectors.toMap(Book::id, Function.identity())));
            indexPosition.compareAndSet(indexPosition.get(), indexPosition.get() + numberOfBooksToBeAddedForIndexing);
        }

        @Override
        public void bootstrap(final Consumer<IndexableDocument> itemConsumer) {
            booksAvailableForIndexing
                    .forEach((key, value) -> itemConsumer.accept(new ForageDocument(key, value.fields())));
        }

        @Override
        public Book get(final String id) {
            return booksAvailableForIndexing.get(id);
        }
    }


    @Test
    void testPeriodicallyUpdatedQueryEngine() throws Exception {
        final DataStore dataStore = new DataStore();
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
}