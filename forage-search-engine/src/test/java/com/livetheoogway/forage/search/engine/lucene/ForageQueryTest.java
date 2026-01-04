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

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.PageQuery;
import com.livetheoogway.forage.models.query.SortCriteria;
import com.livetheoogway.forage.models.query.SortOrder;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.query.search.score.RandomScoreFunction;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.search.engine.ResourceReader;
import com.livetheoogway.forage.search.engine.ResultUtil;
import com.livetheoogway.forage.search.engine.TestUtils;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.Book;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class ForageQueryTest {
    private static ForageLuceneSearchEngine<Book> searchEngine;

    @BeforeAll
    static void setup() throws ForageSearchError, IOException {
        InMemoryHashStore<Book> dataStore = new InMemoryHashStore<>();
        searchEngine = ForageSearchEngineBuilder.<Book>builder()
                .withObjectMapper(TestUtils.mapper())
                .withDataStore(dataStore).build();

        final List<Book> books = ResourceReader.extractBooks();
        final List<IndexableDocument> documents = books
                .stream()
                .map(book -> ForageDocument.builder()
                        .fields(book.fields())
                        .id(book.id())
                        .build())
                .collect(Collectors.toList());
        dataStore.store(books);
        System.out.println("documents.size() = " + documents.size());
        searchEngine.index(documents);
        searchEngine.flush();
    }

    @Test
    void testSearchResultWithTermMatch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.matchQuery("author", "rowling").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(result.getTotal().getTotal() > 10);
        Assertions.assertNotNull(result.getNextPage());
    }

    @Test
    void testPaginatedSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.matchQuery("author", "rowling").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(25, result.getTotal().getTotal());
        result = searchEngine.search(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(25, result.getTotal().getTotal());
        result = searchEngine.search(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(5, result.getMatchingResults().size());
        Assertions.assertEquals(25, result.getTotal().getTotal());
    }

    @Test
    void testMultipleSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.matchQuery("author", "rowling").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        result = searchEngine.search(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        result = searchEngine.search(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(5, result.getMatchingResults().size());

        /* perform next query */
        result = searchEngine.search(
                QueryBuilder.booleanQuery()
                        .query(new MatchQuery("author", "rowling"))
                        .query(new MatchQuery("title", "prince"))
                        .clauseType(ClauseType.MUST)
                        .buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(2, result.getMatchingResults().size());
    }

    @Test
    void testMustClauseSearch() throws ForageSearchError {
        ForageQueryResult<Book> result;
        result = searchEngine.search(
                QueryBuilder.booleanQuery()
                        .query(new MatchQuery("author", "rowling"))
                        .query(new MatchQuery("title", "prince"))
                        .clauseType(ClauseType.MUST)
                        .buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(2, result.getMatchingResults().size());
    }

    @Test
    void testShouldClauseSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.booleanQuery()
                        .query(new MatchQuery("author", "rowling"))
                        .query(new MatchQuery("title", "prince"))
                        .clauseType(ClauseType.SHOULD)
                        .buildForageQuery());

        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(10 < result.getTotal().getTotal());
    }

    @Test
    void testIntRangeSearch() throws ForageSearchError {
        ForageQueryResult<Book> result;

        result = searchEngine.search(QueryBuilder.intRangeQuery("numPage", 600, 800).buildForageQuery());
        Assertions.assertTrue(result.getMatchingResults()
                                      .stream()
                                      .map(matchingResult -> matchingResult.getData().getNumPage())
                                      .allMatch(pages -> pages <= 800 && pages >= 600));
        System.out.println("result.getTotal().getTotal() = " + result.getTotal().getTotal());
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(644, result.getTotal().getTotal());


        result = searchEngine.search(QueryBuilder.intRangeQuery("numPage", 100, 200).buildForageQuery());
        Assertions.assertTrue(result.getMatchingResults()
                                      .stream()
                                      .map(matchingResult -> matchingResult.getData().getNumPage())
                                      .allMatch(pages -> pages >= 100 && pages <= 200));
        System.out.println("result.getTotal().getTotal() = " + result.getTotal().getTotal());
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(1001, result.getTotal().getTotal());
    }

    @Test
    void testFuzzyMatchSearch() throws ForageSearchError {

        /* Match query for sayyer, should give 0 results */
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.matchQuery("title", "sayyer").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(0, result.getMatchingResults().size());
        Assertions.assertEquals(0, result.getTotal().getTotal());

        /* Fuzzy Match query for sayyer should give "tom sawyer" type results */
        result = searchEngine.search(QueryBuilder.fuzzyMatchQuery("title", "sayyer").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertFalse(result.getMatchingResults().isEmpty());
    }

    @Test
    void testPrefixMatchSearch() throws ForageSearchError {

        /* Match query for treas, should give 0 results */
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.matchQuery("title", "treas").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(0, result.getMatchingResults().size());
        Assertions.assertEquals(0, result.getTotal().getTotal());

        /* Prefix Match query for treas should give "treasure island" type results */
        result = searchEngine.search(QueryBuilder.prefixMatchQuery("title", "treas").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertFalse(result.getMatchingResults().isEmpty());
    }

    @Test
    void testPhraseMatchSearch() throws ForageSearchError {

        /* Match query for phrase Tom Sawyer, should give 0 results */
        ForageQueryResult<Book> result =
                searchEngine.search(QueryBuilder.phraseMatchQuery("title", "Tom Sawyer").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(6, result.getMatchingResults().size());
        Assertions.assertEquals(6, result.getTotal().getTotal());
    }

    @Test
    void testAllMatchSearch() throws ForageSearchError {

        /* Match all query should give all books */
        ForageQueryResult<Book> result =
                searchEngine.search(QueryBuilder.matchAllQuery().buildForageQuery());
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(1001, result.getTotal().getTotal());
    }

    @Test
    void testMatchQueryWithRanking() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.search(
                QueryBuilder.phraseMatchQuery("title", "The lord of the rings").buildForageQuery(34));
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(34, result.getMatchingResults().size());
        Assertions.assertEquals(34, result.getTotal().getTotal());

        /* query which ranks results with higher ratings  */
        final var rankingQuery = QueryBuilder.functionScoreQuery()
                .baseQuery(QueryBuilder.phraseMatchQuery("title", "The lord of the rings").build())
//                .scoreFunction(new FieldValueFactorFunction("rating", 1.2f))
                .scoreFunction(new RandomScoreFunction(213L, "rating"))
                .buildForageQuery(34, List.of(SortCriteria.byScore(SortOrder.DESC)));

        System.out.println("----- Ranking Query Results -----");
        result = searchEngine.search(rankingQuery);
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(34, result.getMatchingResults().size());
        Assertions.assertEquals(34, result.getTotal().getTotal());
        checkIfResultHasFirstBookWithHigherRating(result);

    }

    private void checkIfResultHasFirstBookWithHigherRating(ForageQueryResult<Book> result) {
        var highestRatingBookId = result.getMatchingResults()
                .stream()
                .sorted((o1, o2) -> o1.getData().getRating() > o2.getData().getRating() ? -1 : 1)
                .map(k -> k.getData().getId())
                .findFirst()
                .orElse("");
        System.out.println("highestRatingBookId = " + highestRatingBookId);
        Assertions.assertEquals(result.getMatchingResults().get(0).getId(), highestRatingBookId,
                                "Expected highest rated book to be first in results");
    }


    @Test
    void testMatchQueryBoostIncreasesScore() throws ForageSearchError {
        assertBoostIncreasesScore(
                QueryBuilder.matchQuery("author", "rowling").buildForageQuery(1),
                QueryBuilder.matchQuery("author", "rowling").boost(2.5f).buildForageQuery(1),
                book("hp-1", "Stone", "rowling", 350));
    }

    @Test
    void testPhraseMatchQueryBoostIncreasesScore() throws ForageSearchError {
        assertBoostIncreasesScore(
                QueryBuilder.phraseMatchQuery("title", "Harry Potter").buildForageQuery(1),
                QueryBuilder.phraseMatchQuery("title", "Harry Potter").boost(3.0f).buildForageQuery(1),
                book("hp-phrase", "Harry Potter and Magic", "rowling", 300));
    }

    @Test
    void testFuzzyMatchQueryBoostIncreasesScore() throws ForageSearchError {
        assertBoostIncreasesScore(
                QueryBuilder.fuzzyMatchQuery("title", "sayyer").buildForageQuery(1),
                QueryBuilder.fuzzyMatchQuery("title", "sayyer").boost(2.0f).buildForageQuery(1),
                book("sawyer", "Tom Sawyer", "twain", 280));
    }

    @Test
    void testPrefixMatchQueryBoostIncreasesScore() throws ForageSearchError {
        assertBoostIncreasesScore(
                QueryBuilder.prefixMatchQuery("author", "row").buildForageQuery(1),
                QueryBuilder.prefixMatchQuery("author", "row").boost(2.0f).buildForageQuery(1),
                book("hp-prefix", "Any Book", "rowling", 320));
    }

    @Test
    void testRangeQueryBoostIncreasesScore() throws ForageSearchError {
        assertBoostIncreasesScore(
                QueryBuilder.intRangeQuery("numPage", 600, 800).buildForageQuery(1),
                QueryBuilder.intRangeQuery("numPage", 600, 800).boost(2.0f).buildForageQuery(1),
                book("large-book", "Big Tome", "author", 700));
    }

    @Test
    void testMatchAllQueryBoostIncreasesScore() throws ForageSearchError {
        assertBoostIncreasesScore(
                QueryBuilder.matchAllQuery().buildForageQuery(1),
                QueryBuilder.matchAllQuery().boost(2.0f).buildForageQuery(1),
                book("a", "Doc A", "author", 200),
                book("b", "Doc B", "author", 210));
    }

    @Test
    void testShouldClauseBoostReordersResults() throws Exception {
        try (ForageLuceneSearchEngine<Book> miniEngine = buildAdHocEngine(
                book("book-alpha", "Alpha Chronicles"),
                book("book-gamma", "Gamma Chronicles"))) {

            ForageQuery unboosted = QueryBuilder.booleanQuery()
                    .query(QueryBuilder.matchQuery("title", "alpha").build())
                    .query(QueryBuilder.matchQuery("title", "gamma").build())
                    .clauseType(ClauseType.SHOULD)
                    .buildForageQuery(2);

            List<String> unboostedOrder = searchIds(miniEngine, unboosted);
            Assertions.assertEquals(Arrays.asList("book-alpha", "book-gamma"), unboostedOrder);

            ForageQuery boostedGamma = QueryBuilder.booleanQuery()
                    .query(QueryBuilder.matchQuery("title", "alpha").build())
                    .query(QueryBuilder.matchQuery("title", "gamma").boost(5.0f).build())
                    .clauseType(ClauseType.SHOULD)
                    .buildForageQuery(2);

            List<String> boostedOrder = searchIds(miniEngine, boostedGamma);
            Assertions.assertEquals(Arrays.asList("book-gamma", "book-alpha"), boostedOrder);
        }
    }

    @Test
    void testPhraseBoostElevatesExactPhraseMatches() throws Exception {
        try (ForageLuceneSearchEngine<Book> miniEngine = buildAdHocEngine(
                book("generic", "Tom Adventures Collection"),
                book("classic", "The Adventures of Tom Sawyer"))) {

            ForageQuery matchOnly = QueryBuilder.matchQuery("title", "tom").buildForageQuery(2);
            Assertions.assertEquals("generic", searchIds(miniEngine, matchOnly).get(0),
                                    "Without boosts the first indexed doc should lead");

            ForageQuery boostedPhrase = QueryBuilder.booleanQuery()
                    .query(QueryBuilder.matchQuery("title", "tom").build())
                    .query(QueryBuilder.phraseMatchQuery("title", "Tom Sawyer").boost(4.0f).build())
                    .clauseType(ClauseType.SHOULD)
                    .buildForageQuery(2);

            Assertions.assertEquals("classic", searchIds(miniEngine, boostedPhrase).get(0),
                                    "Phrase boost should lift the exact phrase match to the top");
        }
    }

    private void assertBoostIncreasesScore(final ForageQuery baseQuery,
                                           final ForageQuery boostedQuery,
                                           final Book... books) throws ForageSearchError {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(books)) {
            final MatchingResult<Book> baseTop = firstMatch(engine, baseQuery);
            final MatchingResult<Book> boostedTop = firstMatch(engine, boostedQuery);
            Assertions.assertEquals(baseTop.getId(), boostedTop.getId(),
                                    "Boosting should not change the top document for identical queries");
            Assertions.assertTrue(boostedTop.getDocScore().getScore() > baseTop.getDocScore().getScore(),
                                  () -> String.format("Expected boosted score > base score but found %f <= %f",
                                                      boostedTop.getDocScore().getScore(),
                                                      baseTop.getDocScore().getScore()));
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to close ad-hoc search engine", ioe);
        }
    }

    private MatchingResult<Book> firstMatch(final ForageQuery query) throws ForageSearchError {
        return firstMatch(searchEngine, query);
    }

    private MatchingResult<Book> firstMatch(final ForageLuceneSearchEngine<Book> engine,
                                            final ForageQuery query) throws ForageSearchError {
        final ForageQueryResult<Book> result = engine.search(query);
        Assertions.assertFalse(result.getMatchingResults().isEmpty(), "Expected at least one match");
        return result.getMatchingResults().get(0);
    }

    private List<String> searchIds(final ForageLuceneSearchEngine<Book> engine,
                                   final ForageQuery query) throws ForageSearchError {
        return engine.search(query)
                .getMatchingResults()
                .stream()
                .map(MatchingResult::getId)
                .collect(Collectors.toList());
    }

    private ForageLuceneSearchEngine<Book> buildAdHocEngine(final Book... books) throws ForageSearchError {
        final InMemoryHashStore<Book> store = new InMemoryHashStore<>();
        final ForageLuceneSearchEngine<Book> engine = ForageSearchEngineBuilder.<Book>builder()
                .withObjectMapper(TestUtils.mapper())
                .withDataStore(store)
                .build();
        final List<Book> bookList = Arrays.asList(books);
        final List<IndexableDocument> documents = bookList.stream()
                .map(book -> ForageDocument.builder()
                        .id(book.id())
                        .fields(book.fields())
                        .build())
                .collect(Collectors.toList());
        store.store(bookList);
        engine.index(documents);
        engine.flush();
        return engine;
    }

    private Book book(final String id, final String title) {
        return book(id, title, "boost-author", 300);
    }

    private Book book(final String id, final String title, final String author, final int numPages) {
        return new Book(id, title, author, 4.5f, "en", numPages);
    }
}
