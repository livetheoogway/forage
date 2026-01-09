/*
 * Copyright 2026. Live the Oogway, Tushar Naik
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
import com.livetheoogway.forage.models.query.search.score.ConstantScoreFunction;
import com.livetheoogway.forage.models.query.search.score.DecayFunction;
import com.livetheoogway.forage.models.query.search.score.DecayType;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import com.livetheoogway.forage.models.query.search.score.RandomScoreFunction;
import com.livetheoogway.forage.models.query.search.score.ScriptScoreFunction;
import com.livetheoogway.forage.models.query.search.score.WeightedScoreFunction;
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
                .map(book -> (IndexableDocument) ForageDocument.builder()
                        .fields(book.fields())
                        .id(book.id())
                        .build())
                .toList();
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

        /* query which ranks results with higher ratings using FieldValueFactorFunction */
        final var rankingQuery = QueryBuilder.functionScoreQuery()
                .baseQuery(QueryBuilder.phraseMatchQuery("title", "The lord of the rings").build())
                .scoreFunction(new FieldValueFactorFunction("rating", 1.0f))
                .buildForageQuery(34, List.of(SortCriteria.byScore(SortOrder.DESC)));

        System.out.println("----- Ranking Query Results -----");
        result = searchEngine.search(rankingQuery);
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(34, result.getMatchingResults().size());
        Assertions.assertEquals(34, result.getTotal().getTotal());
        assertResultOrderOnRating(result, SortOrder.DESC);

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

    // ==================== CONSTANT SCORE FUNCTION TESTS ====================

    @Test
    void testConstantScoreProducesExactConstantValue() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                book("book-1", "Magic Adventure"),
                book("book-2", "Magic Journey"))) {

            // Constant score should produce the exact constant value as the score
            // (ignoring base relevance score)
            float constantValue = 3.5f;
            ForageQuery constantScoreQuery = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "magic").build())
                    .scoreFunction(new ConstantScoreFunction(constantValue))
                    .buildForageQuery(2);

            ForageQueryResult<Book> result = engine.search(constantScoreQuery);
            float score = result.getMatchingResults().get(0).getDocScore().getScore();

            // Score should be exactly the constant value
            Assertions.assertEquals(constantValue, score, 0.001f,
                                    "Constant score should produce the exact constant value");
        }
    }

    @Test
    void testConstantScoreProducesUniformScoresForAllMatches() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                book("book-1", "Fantasy World"),
                book("book-2", "Fantasy Land"),
                book("book-3", "Fantasy Realm"))) {

            float constantValue = 5.0f;
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "fantasy").build())
                    .scoreFunction(new ConstantScoreFunction(constantValue))
                    .buildForageQuery(3);

            ForageQueryResult<Book> result = engine.search(query);
            Assertions.assertEquals(3, result.getMatchingResults().size());

            // All scores should be exactly the constant value (ignores base relevance)
            for (MatchingResult<Book> match : result.getMatchingResults()) {
                Assertions.assertEquals(constantValue, match.getDocScore().getScore(), 0.001f,
                                        "All matches should have the exact constant score value");
            }
        }
    }

    // ==================== WEIGHTED SCORE FUNCTION TESTS ====================

    @Test
    void testWeightedScoreFunctionMultipliesBaseScore() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                book("book-1", "Science Fiction"),
                book("book-2", "Science Mystery"))) {

            ForageQuery baseQuery = QueryBuilder.matchQuery("title", "science").buildForageQuery(2);
            ForageQueryResult<Book> baseResult = engine.search(baseQuery);
            float baseScore = baseResult.getMatchingResults().get(0).getDocScore().getScore();

            float weight = 2.5f;
            ForageQuery weightedQuery = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "science").build())
                    .scoreFunction(new WeightedScoreFunction(weight))
                    .buildForageQuery(2);

            ForageQueryResult<Book> result = engine.search(weightedQuery);
            float weightedScore = result.getMatchingResults().get(0).getDocScore().getScore();

            Assertions.assertEquals(baseScore * weight, weightedScore, 0.001f,
                                    "Weighted score should multiply base score by weight");
        }
    }

    // ==================== FIELD VALUE FACTOR FUNCTION TESTS ====================

    @Test
    void testFieldValueFactorWithFactorMultiplier() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("low-rated", "Adventure Book", 2.0f, 200),
                bookWithRating("high-rated", "Adventure Tale", 4.0f, 200))) {

            // Factor of 2.0 should double the field values
            float factor = 2.0f;
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "adventure").build())
                    .scoreFunction(new FieldValueFactorFunction("rating", factor))
                    .buildForageQuery(2);

            ForageQueryResult<Book> result = engine.search(query);

            MatchingResult<Book> highRated = result.getMatchingResults().stream()
                    .filter(m -> m.getId().equals("high-rated"))
                    .findFirst().orElseThrow();
            MatchingResult<Book> lowRated = result.getMatchingResults().stream()
                    .filter(m -> m.getId().equals("low-rated"))
                    .findFirst().orElseThrow();

            // Scores should be rating * factor
            Assertions.assertEquals(4.0f * factor, highRated.getDocScore().getScore(), 0.001f);
            Assertions.assertEquals(2.0f * factor, lowRated.getDocScore().getScore(), 0.001f);
        }
    }

    @Test
    void testFieldValueFactorRanksHigherRatingsFirst() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-low", "Mystery Novel", 3.0f, 300),
                bookWithRating("book-mid", "Mystery Story", 4.0f, 300),
                bookWithRating("book-high", "Mystery Tale", 5.0f, 300))) {

            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "mystery").build())
                    .scoreFunction(new FieldValueFactorFunction("rating"))
                    .buildForageQuery(3, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // Highest rated book should be first
            Assertions.assertEquals("book-high", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals("book-mid", result.getMatchingResults().get(1).getId());
            Assertions.assertEquals("book-low", result.getMatchingResults().get(2).getId());
        }
    }

    @Test
    void testFieldValueFactorWithNumPagesField() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("short-book", "History Brief", 4.0f, 100),
                bookWithRating("long-book", "History Complete", 4.0f, 500))) {

            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "history").build())
                    .scoreFunction(new FieldValueFactorFunction("numPage"))
                    .buildForageQuery(2, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // Longer book should have higher score
            Assertions.assertEquals("long-book", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals(500.0f, result.getMatchingResults().get(0).getDocScore().getScore(), 0.001f);
            Assertions.assertEquals(100.0f, result.getMatchingResults().get(1).getDocScore().getScore(), 0.001f);
        }
    }

    // ==================== SCRIPT SCORE FUNCTION TESTS ====================

    @Test
    void testScriptScoreWithSimpleExpression() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-1", "Drama One", 2.0f, 100),
                bookWithRating("book-2", "Drama Two", 4.0f, 200))) {

            // Script: rating + numPage (adds field values)
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "drama").build())
                    .scoreFunction(new ScriptScoreFunction("rating + numPage"))
                    .buildForageQuery(2, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // let us obtain the base score for validation
            ForageQuery baseQuery = QueryBuilder.matchQuery("title", "drama").buildForageQuery(2);
            ForageQueryResult<Book> baseResult = engine.search(baseQuery);
            float baseScore = baseResult.getMatchingResults().get(0).getDocScore().getScore();

            // book-2: 4.0 + 200 = 204 (multiplied by base score)
            // book-1: 2.0 + 100 = 102 (multiplied by base score of 0.082873434)
            Assertions.assertEquals("book-2", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals("book-1", result.getMatchingResults().get(1).getId());
            Assertions.assertEquals(204.0f * baseScore, result.getMatchingResults().get(0).getDocScore().getScore(),
                                    0.001f);
            Assertions.assertEquals(102.0f * baseScore, result.getMatchingResults().get(1).getDocScore().getScore(),
                                    0.001f);
        }
    }

    @Test
    void testScriptScoreWithMathFunctions() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-small", "Thriller Short", 4.0f, 100),
                bookWithRating("book-large", "Thriller Long", 4.0f, 10000))) {

            // Script using ln function: ln(1 + numPage) - dampens large values
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "thriller").build())
                    .scoreFunction(new ScriptScoreFunction("ln(1 + numPage)"))
                    .buildForageQuery(2, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // ln(1 + 10000) > ln(1 + 100), so book-large should still be first
            Assertions.assertEquals("book-large", result.getMatchingResults().get(0).getId());

            // But the difference should be dampened
            float scoreLarge = result.getMatchingResults().get(0).getDocScore().getScore();
            float scoreSmall = result.getMatchingResults().get(1).getDocScore().getScore();
            float ratio = scoreLarge / scoreSmall;

            // Without ln: ratio would be ~100; with ln: ratio should be much smaller
            Assertions.assertTrue(ratio < 3, "ln function should dampen the score difference");
        }
    }

    @Test
    void testScriptScoreWithScoreVariable() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-1", "Romance Story", 2.0f, 100),
                bookWithRating("book-2", "Romance Novel", 4.0f, 100))) {

            // Script that combines base score with rating
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "romance").build())
                    .scoreFunction(new ScriptScoreFunction("score * rating"))
                    .buildForageQuery(2, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // book-2 has higher rating, so score * 4.0 > score * 2.0
            Assertions.assertEquals("book-2", result.getMatchingResults().get(0).getId());
        }
    }

    // ==================== RANDOM SCORE FUNCTION TESTS ====================

    @Test
    void testRandomScoreDeterminismWithSameSeed() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-1", "Horror Tale", 4.0f, 200),
                bookWithRating("book-2", "Horror Story", 3.5f, 250),
                bookWithRating("book-3", "Horror Novel", 4.5f, 180))) {

            long seed = 42L;

            // First search with seed
            ForageQuery query1 = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "horror").build())
                    .scoreFunction(new RandomScoreFunction(seed, "rating"))
                    .buildForageQuery(3);
            ForageQueryResult<Book> result1 = engine.search(query1);

            // Second search with same seed
            ForageQuery query2 = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "horror").build())
                    .scoreFunction(new RandomScoreFunction(seed, "rating"))
                    .buildForageQuery(3);
            ForageQueryResult<Book> result2 = engine.search(query2);

            // Results should be in the same order
            for (int i = 0; i < result1.getMatchingResults().size(); i++) {
                Assertions.assertEquals(
                        result1.getMatchingResults().get(i).getId(),
                        result2.getMatchingResults().get(i).getId(),
                        "Same seed should produce same ordering");
                Assertions.assertEquals(
                        result1.getMatchingResults().get(i).getDocScore().getScore(),
                        result2.getMatchingResults().get(i).getDocScore().getScore(),
                        0.001f,
                        "Same seed should produce same scores");
            }
        }
    }

    @Test
    void testRandomScoreDifferentSeedsProduceDifferentOrdering() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-1", "Comedy Show", 4.0f, 200),
                bookWithRating("book-2", "Comedy Night", 4.0f, 200),
                bookWithRating("book-3", "Comedy Hour", 4.0f, 200))) {

            ForageQuery query1 = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "comedy").build())
                    .scoreFunction(new RandomScoreFunction(100L, "numPage"))
                    .buildForageQuery(3);
            ForageQueryResult<Book> result1 = engine.search(query1);

            ForageQuery query2 = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "comedy").build())
                    .scoreFunction(new RandomScoreFunction(999L, "numPage"))
                    .buildForageQuery(3);
            ForageQueryResult<Book> result2 = engine.search(query2);

            // Different seeds should produce different scores
            boolean anyDifferent = false;
            for (int i = 0; i < result1.getMatchingResults().size(); i++) {
                float score1 = result1.getMatchingResults().get(i).getDocScore().getScore();
                float score2 = result2.getMatchingResults().get(i).getDocScore().getScore();
                if (Math.abs(score1 - score2) > 0.001f) {
                    anyDifferent = true;
                    break;
                }
            }
            Assertions.assertTrue(anyDifferent, "Different seeds should produce different scores");
        }
    }

    // ==================== DECAY FUNCTION TESTS ====================

    @Test
    void testDecayFunctionGaussian() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("near-origin", "Biography Close", 4.0f, 100),
                bookWithRating("far-origin", "Biography Far", 4.0f, 500))) {

            // Gaussian decay: origin=100, scale=100, decay=0.5
            // Books closer to origin (100 pages) should score higher
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "biography").build())
                    .scoreFunction(new DecayFunction(100.0, 100.0, 0.0, 0.5, DecayType.GAUSS, "numPage"))
                    .buildForageQuery(2, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // Book at origin (100 pages) should have higher score
            Assertions.assertEquals("near-origin", result.getMatchingResults().get(0).getId());

            float nearScore = result.getMatchingResults().get(0).getDocScore().getScore();
            float farScore = result.getMatchingResults().get(1).getDocScore().getScore();
            Assertions.assertTrue(nearScore > farScore,
                                  "Gaussian decay should score nearer documents higher");
        }
    }

    @Test
    void testDecayFunctionExponential() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("close", "Memoir Recent", 4.0f, 50),
                bookWithRating("medium", "Memoir Middle", 4.0f, 150),
                bookWithRating("far", "Memoir Old", 4.0f, 300))) {

            // Exponential decay: origin=0, scale=100
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "memoir").build())
                    .scoreFunction(new DecayFunction(0.0, 100.0, 0.0, 0.5, DecayType.EXP, "numPage"))
                    .buildForageQuery(3, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // Closer to origin (0 pages) should score higher
            Assertions.assertEquals("close", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals("medium", result.getMatchingResults().get(1).getId());
            Assertions.assertEquals("far", result.getMatchingResults().get(2).getId());
        }
    }

    @Test
    void testDecayFunctionLinear() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("at-origin", "Documentary Zero", 4.0f, 100),
                bookWithRating("away", "Documentary Far", 4.0f, 200))) {

            // Linear decay: origin=100, scale=100
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "documentary").build())
                    .scoreFunction(new DecayFunction(100.0, 100.0, 0.0, 0.5, DecayType.LINEAR, "numPage"))
                    .buildForageQuery(2, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            Assertions.assertEquals("at-origin", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals("away", result.getMatchingResults().get(1).getId());
        }
    }

    @Test
    void testDecayFunctionWithOffset() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("within-offset", "Guide Basic", 4.0f, 90),
                bookWithRating("at-origin", "Guide Start", 4.0f, 100),
                bookWithRating("beyond-offset", "Guide Advanced", 4.0f, 200))) {

            // Decay with offset=50: documents within 50 of origin get full score
            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "guide").build())
                    .scoreFunction(new DecayFunction(100.0, 100.0, 50.0, 0.5, DecayType.EXP, "numPage"))
                    .buildForageQuery(3, List.of(SortCriteria.byScore(SortOrder.DESC)));

            ForageQueryResult<Book> result = engine.search(query);

            // Books within offset (90, 100) should have same/similar high scores
            float originScore = result.getMatchingResults().stream()
                    .filter(m -> m.getId().equals("at-origin"))
                    .findFirst().map(res -> res.getDocScore().getScore()).orElseThrow();
            float withinOffsetScore = result.getMatchingResults().stream()
                    .filter(m -> m.getId().equals("within-offset"))
                    .findFirst().map(res -> res.getDocScore().getScore()).orElseThrow();
            float beyondOffsetScore = result.getMatchingResults().stream()
                    .filter(m -> m.getId().equals("beyond-offset"))
                    .findFirst().map(res -> res.getDocScore().getScore()).orElseThrow();

            Assertions.assertEquals(originScore, withinOffsetScore, 0.001f,
                                    "Within offset should have same score as origin");
            Assertions.assertTrue(originScore > beyondOffsetScore,
                                  "Beyond offset should have lower score");
        }
    }

    // ==================== COMBINED SCORING AND SORTING TESTS ====================

    @Test
    void testFunctionScoreWithAscendingSort() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("high", "Action High", 5.0f, 200),
                bookWithRating("low", "Action Low", 1.0f, 200),
                bookWithRating("mid", "Action Mid", 3.0f, 200))) {

            ForageQuery query = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "action").build())
                    .scoreFunction(new FieldValueFactorFunction("rating"))
                    .buildForageQuery(3, List.of(SortCriteria.byScore(SortOrder.ASC)));

            ForageQueryResult<Book> result = engine.search(query);

            // With ascending sort, lowest score (lowest rating) should be first
            Assertions.assertEquals("low", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals("mid", result.getMatchingResults().get(1).getId());
            Assertions.assertEquals("high", result.getMatchingResults().get(2).getId());
        }
    }

    @Test
    void testFunctionScoreWithTopLevelBoost() throws Exception {
        try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                bookWithRating("book-1", "Thriller Chase", 4.0f, 200))) {

            // Without boost
            ForageQuery queryNoBoost = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "thriller").build())
                    .scoreFunction(new FieldValueFactorFunction("rating"))
                    .buildForageQuery(1);

            ForageQueryResult<Book> resultNoBoost = engine.search(queryNoBoost);
            float scoreNoBoost = resultNoBoost.getMatchingResults().get(0).getDocScore().getScore();

            // With boost of 2.0
            ForageQuery queryWithBoost = QueryBuilder.functionScoreQuery()
                    .baseQuery(QueryBuilder.matchQuery("title", "thriller").build())
                    .scoreFunction(new FieldValueFactorFunction("rating"))
                    .boost(2.0f)
                    .buildForageQuery(1);

            ForageQueryResult<Book> resultWithBoost = engine.search(queryWithBoost);
            float scoreWithBoost = resultWithBoost.getMatchingResults().get(0).getDocScore().getScore();

            Assertions.assertEquals(scoreNoBoost * 2.0f, scoreWithBoost, 0.001f,
                                    "Top-level boost should multiply the function score");
        }
    }

    @Test
    void testFunctionScoreRankingOnLargeCatalog() throws ForageSearchError {
        // Test on the main searchEngine with full book catalog
        ForageQuery rankingQuery = QueryBuilder.functionScoreQuery()
                .baseQuery(QueryBuilder.matchQuery("author", "rowling").build())
                .scoreFunction(new FieldValueFactorFunction("rating"))
                .buildForageQuery(25, List.of(SortCriteria.byScore(SortOrder.DESC)));

        ForageQueryResult<Book> result = searchEngine.search(rankingQuery);
        System.out.println("=== Large Catalog Ranking Test ===");
        System.out.println(ResultUtil.getBookRepresentation(result));

        assertResultOrderOnRating(result, SortOrder.DESC);
    }

    @Test
    void testAllDecayTypesProduceValidScores() throws Exception {
        for (DecayType decayType : DecayType.values()) {
            try (ForageLuceneSearchEngine<Book> engine = buildAdHocEngine(
                    bookWithRating("test-book", "Test " + decayType.name(), 4.0f, 150))) {

                ForageQuery query = QueryBuilder.functionScoreQuery()
                        .baseQuery(QueryBuilder.matchQuery("title", "test").build())
                        .scoreFunction(new DecayFunction(100.0, 100.0, 0.0, 0.5, decayType, "numPage"))
                        .buildForageQuery(1);

                ForageQueryResult<Book> result = engine.search(query);
                Assertions.assertFalse(result.getMatchingResults().isEmpty(),
                                       "Should have results for decay type: " + decayType);

                float score = result.getMatchingResults().get(0).getDocScore().getScore();
                Assertions.assertFalse(Float.isNaN(score),
                                       "Score should not be NaN for decay type: " + decayType);
                Assertions.assertTrue(score > 0,
                                      "Score should be positive for decay type: " + decayType);
            }
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

    private void assertResultOrderOnRating(final ForageQueryResult<Book> result, final SortOrder sortOrder) {
        Assertions.assertFalse(result.getMatchingResults().isEmpty());

        // Verify results are sorted by rating
        float previousRating = sortOrder == SortOrder.DESC
                ? Float.MAX_VALUE
                : 0;
        for (MatchingResult<Book> match : result.getMatchingResults()) {
            float currentRating = match.getData().getRating();
            if (sortOrder == SortOrder.DESC) {
                Assertions.assertTrue(currentRating <= previousRating,
                                      "Results should be sorted by rating descending");
            } else {
                Assertions.assertTrue(currentRating <= previousRating,
                                      "Results should be sorted by rating ascending");
            }
            previousRating = currentRating;
        }
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
                .toList();
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

    private Book bookWithRating(final String id, final String title, final float rating, final int numPages) {
        return new Book(id, title, "test-author", rating, "en", numPages);
    }
}
