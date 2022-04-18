package com.phonepe.platform.forage.search.engine.lucene;

import com.google.common.collect.Lists;
import com.phonepe.platform.forage.core.AsyncQueuedConsumer;
import com.phonepe.platform.forage.core.Bootstrapper;
import com.phonepe.platform.forage.core.PeriodicUpdateEngine;
import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.search.engine.ResourceReader;
import com.phonepe.platform.forage.search.engine.TestUtils;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.Book;
import com.phonepe.platform.forage.search.engine.model.index.ForageDocument;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;
import com.phonepe.platform.forage.search.engine.model.query.ForageSearchQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.RangeQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.range.IntRange;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class PeriodicallyUpdatedLuceneQueryEngineContainerTest {

    private static class DataStore implements Bootstrapper<Book, IndexableDocument<Book>> {
        private final AtomicInteger indexPosition;
        private final List<Book> booksAvailableForIndexing;
        private final List<Book> fullGlossaryOfBook;

        public DataStore() throws IOException {
            this.fullGlossaryOfBook = ResourceReader.extractBooks();
            this.indexPosition = new AtomicInteger(0);
            this.booksAvailableForIndexing = Lists.newArrayList();
        }

        public void addBooks(int numberOfBooksToBeAddedForIndexing) {
            this.booksAvailableForIndexing.addAll(
                    fullGlossaryOfBook.subList(indexPosition.get(),
                                               indexPosition.get() + numberOfBooksToBeAddedForIndexing));
            indexPosition.compareAndSet(indexPosition.get(), indexPosition.get() + numberOfBooksToBeAddedForIndexing);
        }

        @Override
        public void bootstrap(final Consumer<IndexableDocument<Book>> itemConsumer) {
            for (final Book book : booksAvailableForIndexing) {
                itemConsumer.accept(new ForageDocument<>(book.getId(), book, book.fields()));
            }
        }
    }


    @Test
    void testPeriodicallyUpdatedQueryEngine() throws Exception {
        final LuceneQueryEngineContainer<Book> luceneQueryEngineContainer = new LuceneQueryEngineContainer<Book>(
                LuceneSearchEngineBuilder.<Book>builder()
                        .withMapper(TestUtils.mapper()));

        final DataStore dataStore = new DataStore();
        dataStore.addBooks(1);
        final PeriodicUpdateEngine<Book, IndexableDocument<Book>> periodicUpdateEngine =
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
                    final ForageQueryResult<Book> query = luceneQueryEngineContainer.query(
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
                    final ForageQueryResult<Book> query = luceneQueryEngineContainer.query(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query.getTotal().getTotal() == 5;
                });

        final ForageQueryResult<Book> query = luceneQueryEngineContainer.query(
                new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
        Assertions.assertEquals(5, query.getTotal().getTotal());

        /* increment the datastore by 2 more books */
        dataStore.addBooks(2);
        Awaitility.await().atMost(Duration.of(5, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .until(() -> {
                    final ForageQueryResult<Book> query2 = luceneQueryEngineContainer.query(
                            new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
                    return query2.getTotal().getTotal() == 7;
                });

        final ForageQueryResult<Book> query2 = luceneQueryEngineContainer.query(
                new ForageSearchQuery(new RangeQuery("numPage", new IntRange(0, 100000)), 10));
        Assertions.assertEquals(7, query2.getTotal().getTotal());

        periodicUpdateEngine.stop();
    }
}