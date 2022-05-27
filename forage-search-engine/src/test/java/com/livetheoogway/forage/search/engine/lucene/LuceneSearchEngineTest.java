package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.query.PageQuery;
import com.livetheoogway.forage.models.query.search.ClauseType;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.util.QueryBuilder;
import com.livetheoogway.forage.search.engine.model.Book;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import com.livetheoogway.forage.search.engine.ResourceReader;
import com.livetheoogway.forage.search.engine.ResultUtil;
import com.livetheoogway.forage.search.engine.TestIdUtils;
import com.livetheoogway.forage.search.engine.TestUtils;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class LuceneSearchEngineTest {
    private static LuceneSearchEngine<Book> searchEngine;

    @BeforeAll
    static void setup() throws ForageSearchError, IOException {
        searchEngine = LuceneSearchEngineBuilder.<Book>builder()
                                        .withMapper(TestUtils.mapper()).build();

        final List<IndexableDocument<Book>> documents = ResourceReader.extractBooks()
                .stream()
                .map(book -> ForageDocument.<Book>builder()
                        .fields(book.fields())
                        .data(book)
                        .id(TestIdUtils.generateBookId())
                        .build())
                .collect(Collectors.toList());

        searchEngine.index(documents);
        searchEngine.flush();
    }

    @Test
    void testSearchResultWithTermMatch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.query(QueryBuilder.matchQuery("author", "rowling").build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(result.getTotal().getTotal() > 10);
        Assertions.assertNotNull(result.getNextPage());
    }

    @Test
    void testPaginatedSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.query(QueryBuilder.matchQuery("author", "rowling").build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(25, result.getTotal().getTotal());
        result = searchEngine.query(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(25, result.getTotal().getTotal());
        result = searchEngine.query(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(5, result.getMatchingResults().size());
        Assertions.assertEquals(25, result.getTotal().getTotal());
    }

    @Test
    void testMultipleSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.query(QueryBuilder.matchQuery("author", "rowling").build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        result = searchEngine.query(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        result = searchEngine.query(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(5, result.getMatchingResults().size());

        /* perform next query */
        result = searchEngine.query(
                QueryBuilder.booleanQuery()
                        .query(new MatchQuery("author", "rowling"))
                        .query(new MatchQuery("title", "prince"))
                        .clauseType(ClauseType.MUST)
                        .build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(2, result.getMatchingResults().size());
    }

    @Test
    void testMustClauseSearch() throws ForageSearchError {
        ForageQueryResult<Book> result;
        result = searchEngine.query(
                QueryBuilder.booleanQuery()
                        .query(new MatchQuery("author", "rowling"))
                        .query(new MatchQuery("title", "prince"))
                        .clauseType(ClauseType.MUST)
                        .build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(2, result.getMatchingResults().size());
    }

    @Test
    void testShouldClauseSearch() throws ForageSearchError {
        ForageQueryResult<Book> result = searchEngine.query(
                QueryBuilder.booleanQuery()
                        .query(new MatchQuery("author", "rowling"))
                        .query(new MatchQuery("title", "prince"))
                        .clauseType(ClauseType.SHOULD)
                        .build());

        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(10 < result.getTotal().getTotal());
    }

    @Test
    void testIntRangeSearch() throws ForageSearchError {
        ForageQueryResult<Book> result;

        result = searchEngine.query(QueryBuilder.intRangeQuery("numPage", 600, 800).build());
        Assertions.assertTrue(result.getMatchingResults()
                                      .stream()
                                      .map(matchingResult -> matchingResult.getData().getNumPage())
                                      .allMatch(pages -> pages <= 800 && pages >= 600));
        System.out.println("result.getTotal().getTotal() = " + result.getTotal().getTotal());
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(644, result.getTotal().getTotal());


        result = searchEngine.query(QueryBuilder.intRangeQuery("numPage", 100, 200).build());
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
        ForageQueryResult<Book> result = searchEngine.query(QueryBuilder.matchQuery("title", "sayyer").build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(0, result.getMatchingResults().size());
        Assertions.assertEquals(0, result.getTotal().getTotal());

        /* Fuzzy Match query for sayyer should give tom sawyer type results */
        result = searchEngine.query(QueryBuilder.fuzzyMatchQuery("title", "sayyer").build());
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertTrue(0 < result.getMatchingResults().size());
    }
}
