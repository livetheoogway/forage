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

import com.livetheoogway.forage.models.query.PageQuery;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
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
        ForageQueryResult<Book> result = searchEngine.search(QueryBuilder.matchQuery("author", "rowling").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(result.getTotal().getTotal() > 10);
        Assertions.assertNotNull(result.getNextPage());
    }

    @Test
    void testPaginatedSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.search(QueryBuilder.matchQuery("author", "rowling").buildForageQuery());
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
        ForageQueryResult<Book> result = searchEngine.search(QueryBuilder.matchQuery("author", "rowling").buildForageQuery());
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
        ForageQueryResult<Book> result = searchEngine.search(QueryBuilder.matchQuery("title", "sayyer").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(0, result.getMatchingResults().size());
        Assertions.assertEquals(0, result.getTotal().getTotal());

        /* Fuzzy Match query for sayyer should give "tom sawyer" type results */
        result = searchEngine.search(QueryBuilder.fuzzyMatchQuery("title", "sayyer").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertTrue(0 < result.getMatchingResults().size());
    }

    @Test
    void testPrefixMatchSearch() throws ForageSearchError {

        /* Match query for treas, should give 0 results */
        ForageQueryResult<Book> result = searchEngine.search(QueryBuilder.matchQuery("title", "treas").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(0, result.getMatchingResults().size());
        Assertions.assertEquals(0, result.getTotal().getTotal());

        /* Prefix Match query for treas should give "treasure island" type results */
        result = searchEngine.search(QueryBuilder.prefixMatchQuery("title", "treas").buildForageQuery());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertTrue(0 < result.getMatchingResults().size());
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
}
