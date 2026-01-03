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
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.search.Query;
import com.livetheoogway.forage.models.query.search.RangeQuery;
import com.livetheoogway.forage.models.query.search.range.IntRange;
import com.livetheoogway.forage.models.query.search.score.DecayFunction;
import com.livetheoogway.forage.models.query.search.score.DecayType;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import com.livetheoogway.forage.models.query.search.score.RandomScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScriptScoreFunction;
import com.livetheoogway.forage.models.query.search.score.WeightedScoreFunction;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

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
    void testErrorsWhenBootstrappingSimultaneously() throws Exception {
        /* This test is being written because we observe an NPE when parallel bootstrap runs happen */

        final BookDataStore dataStore = new BookDataStore();
        final ForageEngineIndexer<Book> luceneQueryEngineContainer = new ForageEngineIndexer<>(
                ForageSearchEngineBuilder.<Book>builder()
                        .withDataStore(dataStore)
                        .withObjectMapper(TestUtils.mapper()));

        dataStore.addBooks(1);

        AtomicBoolean wasErrorTrapped = new AtomicBoolean(false);

        /* we will set up a trap using the error handler in AsyncQueuedConsumer */
        final PeriodicUpdateEngine<IndexableDocument> periodicUpdateEngine =
                new PeriodicUpdateEngine<>(
                        dataStore, new AsyncQueuedConsumer<>(luceneQueryEngineContainer, 10,
                                                             (indexableDocument, e) -> wasErrorTrapped.set(true)),
                        1, TimeUnit.SECONDS
                );

        /* while the periodic update happens in background (and calls the bootstrap), we will invoke bootstrap
        forcefully */
        periodicUpdateEngine.start();
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

        if (wasErrorTrapped.get()) {
            Assertions.fail("There was an error in the async handler");
        }

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

    private void performParallelSearchExecutions(final BookDataStore dataStore,
                                                 final ForageEngineIndexer<Book> luceneQueryEngineContainer)
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

    @Test
    void testFunctionScoreRankingAcrossFullCatalog() throws Exception {
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
        waitForTotalDocuments(luceneQueryEngineContainer, 1);

        periodicUpdateEngine.start();
        dataStore.addAllBooks();
        waitForAuthorResults(luceneQueryEngineContainer, "rowling", 20);

        assertConstantScoreFunction(luceneQueryEngineContainer);
        assertWeightedScoreScaling(luceneQueryEngineContainer);
        assertFieldValueFactorOrdering(luceneQueryEngineContainer);
        assertScriptScoreExpression(luceneQueryEngineContainer);
        assertRandomScoreDeterminism(luceneQueryEngineContainer);
        assertDecayFunctionBias(luceneQueryEngineContainer);

        periodicUpdateEngine.stop();
    }

    private void waitForTotalDocuments(final ForageEngineIndexer<Book> searchEngine, final int expectedTotal) {
        Awaitility.await().atMost(Duration.of(60, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .ignoreExceptionsMatching(throwable -> throwable instanceof ForageSearchError
                        && ((ForageSearchError) throwable).getForageErrorCode()
                        == ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET)
                .until(() -> {
                    final ForageQueryResult<Book> query = searchEngine.search(
                            QueryBuilder.matchAllQuery().buildForageQuery());
                    return query.getTotal().getTotal() >= expectedTotal;
                });
    }

    private void waitForAuthorResults(final ForageEngineIndexer<Book> searchEngine,
                                      final String author,
                                      final int expectedTotal) {
        Awaitility.await().atMost(Duration.of(60, ChronoUnit.SECONDS))
                .with()
                .pollInterval(Duration.of(100, ChronoUnit.MILLIS))
                .ignoreExceptionsMatching(throwable -> throwable instanceof ForageSearchError
                        && ((ForageSearchError) throwable).getForageErrorCode()
                        == ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET)
                .until(() -> {
                    final ForageQueryResult<Book> query = searchEngine.search(
                            QueryBuilder.matchQuery("author", author).buildForageQuery());
                    return query.getTotal().getTotal() >= expectedTotal;
                });
    }

    private void assertConstantScoreFunction(final ForageEngineIndexer<Book> searchEngine) throws ForageSearchError {
        final float constantScore = 7.5f;
        final ForageQuery baselineQuery = QueryBuilder.matchQuery("author", "rowling").buildForageQuery(5);
        final ForageQueryResult<Book> baselineResult = searchEngine.search(baselineQuery);
        final ForageQuery query = QueryBuilder.functionScoreQuery()
                .baseQuery(QueryBuilder.matchQuery("author", "rowling").build())
                .constantScore(constantScore)
                .buildForageQuery(5);

        final ForageQueryResult<Book> result = searchEngine.search(query);
        Assertions.assertFalse(result.getMatchingResults().isEmpty());
        for (int i = 0; i < result.getMatchingResults().size(); i++) {
            final MatchingResult<Book> boosted = result.getMatchingResults().get(i);
            final MatchingResult<Book> baseline = baselineResult.getMatchingResults().get(i);
            Assertions.assertEquals(baseline.getId(), boosted.getId());
            final float expectedScore = baseline.getDocScore().getScore() * constantScore;
            Assertions.assertEquals(expectedScore,
                                    boosted.getDocScore().getScore(),
                                    1.0e-3f * Math.max(1f, expectedScore));
        }
    }

    private void assertWeightedScoreScaling(final ForageEngineIndexer<Book> searchEngine) throws ForageSearchError {
        final ForageQuery baselineQuery = QueryBuilder.matchQuery("author", "rowling").buildForageQuery(5);
        final ForageQueryResult<Book> baselineResult = searchEngine.search(baselineQuery);

        final float weight = 3.0f;
        final ForageQuery weightedQuery = buildFunctionScoreQuery(
                QueryBuilder.matchQuery("author", "rowling").build(),
                new WeightedScoreFunction(weight),
                5);
        final ForageQueryResult<Book> weightedResult = searchEngine.search(weightedQuery);

        Assertions.assertEquals(baselineResult.getMatchingResults().size(),
                                weightedResult.getMatchingResults().size());

        for (int i = 0; i < baselineResult.getMatchingResults().size(); i++) {
            final MatchingResult<Book> base = baselineResult.getMatchingResults().get(i);
            final MatchingResult<Book> weighted = weightedResult.getMatchingResults().get(i);
            Assertions.assertEquals(base.getId(), weighted.getId());
            final float expectedScore = base.getDocScore().getScore() * weight;
            Assertions.assertEquals(expectedScore,
                                    weighted.getDocScore().getScore(),
                                    1.0e-3f * Math.max(1f, expectedScore));
        }
    }

    private void assertFieldValueFactorOrdering(final ForageEngineIndexer<Book> searchEngine)
            throws ForageSearchError {
        final ForageQueryResult<Book> result = searchEngine.search(
                buildFunctionScoreQuery(QueryBuilder.matchAllQuery().build(),
                                        new FieldValueFactorFunction("rating"),
                                        10));

        assertSortedDescending(result.getMatchingResults(), Book::getRating);
    }

    private void assertScriptScoreExpression(final ForageEngineIndexer<Book> searchEngine) throws ForageSearchError {
        final String script = "score + rating * 2 - numPage / 1000";
        final ForageQuery query = buildFunctionScoreQuery(QueryBuilder.matchAllQuery().build(),
                                                          new ScriptScoreFunction(script),
                                                          1);
        final ForageQueryResult<Book> result = searchEngine.search(query);
        Assertions.assertFalse(result.getMatchingResults().isEmpty());

        final MatchingResult<Book> topResult = result.getMatchingResults().get(0);
        final double expectedScore =
                1.0 + topResult.getData().getRating() * 2.0 - (double) topResult.getData().getNumPage() / 1000.0;
        Assertions.assertEquals(expectedScore, topResult.getDocScore().getScore(), 1e-3);
    }

    private void assertRandomScoreDeterminism(final ForageEngineIndexer<Book> searchEngine)
            throws ForageSearchError {
        final long seed = 99L;
        final ForageQuery deterministicQuery = buildFunctionScoreQuery(
                QueryBuilder.matchAllQuery().build(),
                new RandomScoreFunction(seed, "numPage"),
                10);

        final List<String> firstRun = extractIds(searchEngine.search(deterministicQuery));
        final List<String> secondRun = extractIds(searchEngine.search(deterministicQuery));
        Assertions.assertEquals(firstRun, secondRun);

        final List<String> differentSeed = extractIds(searchEngine.search(
                buildFunctionScoreQuery(QueryBuilder.matchAllQuery().build(),
                                        new RandomScoreFunction(seed + 1, "numPage"),
                                        10)));
        Assertions.assertNotEquals(firstRun, differentSeed);
    }

    private void assertDecayFunctionBias(final ForageEngineIndexer<Book> searchEngine) throws ForageSearchError {
        final DecayFunction decayFunction =
                new DecayFunction(0.0, 200.0, 0.0, 0.5, DecayType.LINEAR, "numPage");
        final ForageQueryResult<Book> result = searchEngine.search(
                buildFunctionScoreQuery(QueryBuilder.matchAllQuery().build(), decayFunction, 5));

        assertSortedAscending(result.getMatchingResults(), book -> book.getNumPage());
    }

    private ForageQuery buildFunctionScoreQuery(final Query baseQuery,
                                                final ScoreFunction scoreFunction,
                                                final int size) {
        return QueryBuilder.functionScoreQuery()
                .baseQuery(baseQuery)
                .scoreFunction(scoreFunction)
                .buildForageQuery(size);
    }

    private List<String> extractIds(final ForageQueryResult<Book> result) {
        return result.getMatchingResults()
                .stream()
                .map(MatchingResult::getId)
                .collect(Collectors.toList());
    }

    private void assertSortedDescending(final List<MatchingResult<Book>> results,
                                        final ToDoubleFunction<Book> extractor) {
        for (int i = 1; i < results.size(); i++) {
            final double prev = extractor.applyAsDouble(results.get(i - 1).getData());
            final double current = extractor.applyAsDouble(results.get(i).getData());
            Assertions.assertTrue(prev + 1e-6 >= current,
                                  () -> String.format("Expected non-increasing ordering but found %f < %f", prev,
                                                      current));
        }
    }

    private void assertSortedAscending(final List<MatchingResult<Book>> results,
                                       final ToDoubleFunction<Book> extractor) {
        for (int i = 1; i < results.size(); i++) {
            final double prev = extractor.applyAsDouble(results.get(i - 1).getData());
            final double current = extractor.applyAsDouble(results.get(i).getData());
            Assertions.assertTrue(prev <= current + 1e-6,
                                  () -> String.format("Expected non-decreasing ordering but found %f > %f", prev,
                                                      current));
        }
    }
}
