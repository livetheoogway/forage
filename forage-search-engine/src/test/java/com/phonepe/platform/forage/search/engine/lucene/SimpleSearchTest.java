package com.phonepe.platform.forage.search.engine.lucene;

import com.google.common.collect.Lists;
import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.search.engine.ForageSearchEngine;
import com.phonepe.platform.forage.search.engine.ResourceReader;
import com.phonepe.platform.forage.search.engine.ResultUtil;
import com.phonepe.platform.forage.search.engine.TestIdUtils;
import com.phonepe.platform.forage.search.engine.TestUtils;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.Book;
import com.phonepe.platform.forage.search.engine.model.index.ForageDocument;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;
import com.phonepe.platform.forage.search.engine.model.query.ForageQuery;
import com.phonepe.platform.forage.search.engine.model.query.ForageSearchQuery;
import com.phonepe.platform.forage.search.engine.model.query.PageQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.BooleanQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.ClauseType;
import com.phonepe.platform.forage.search.engine.model.query.search.MatchQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.RangeQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.range.IntRange;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class SimpleSearchTest {
    private static ForageSearchEngine<IndexableDocument, ForageQuery, ForageQueryResult> searchEngine;

    @BeforeAll
    static void setup() throws ForageSearchError, IOException {
        searchEngine = LuceneSearchEngineBuilder.builder()
                .withMapper(TestUtils.mapper()).build();

        final List<IndexableDocument> documents = ResourceReader.extractBooks()
                .stream()
                .map(book -> ForageDocument.builder()
                        .fields(book.fields())
                        .data(book)
                        .id(TestIdUtils.generateBookId())
                        .build())
                .collect(Collectors.toList());

        searchEngine.index(documents);
        searchEngine.flush();
    }

    @Test
    void testSimpleSearch() throws ForageSearchError {
        ForageQueryResult result = searchEngine.query(new ForageSearchQuery(new MatchQuery("author", "rowling"), 10));
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(result.getTotal().getTotal() > 10);
        Assertions.assertNotNull(result.getNextPage());
    }

    @Test
    void testPaginatedSearch() throws ForageSearchError {
        ForageQueryResult result = searchEngine.query(new ForageSearchQuery(new MatchQuery("author", "rowling"), 10));
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
        ForageQueryResult result = searchEngine.query(new ForageSearchQuery(new MatchQuery("author", "rowling"), 10));
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        result = searchEngine.query(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(10, result.getMatchingResults().size());
        result = searchEngine.query(new PageQuery(result.getNextPage(), 10));
        Assertions.assertEquals(5, result.getMatchingResults().size());

        result = searchEngine.query(new ForageSearchQuery(
                new BooleanQuery(Lists.newArrayList(
                        new MatchQuery("author", "rowling"),
                        new MatchQuery("title", "prince")),
                                 ClauseType.MUST),
                10));
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(2, result.getMatchingResults().size());
    }

    @Test
    void testMustClauseSearch() throws ForageSearchError {
        ForageQueryResult result;
        result = searchEngine.query(new ForageSearchQuery(
                new BooleanQuery(Lists.newArrayList(
                        new MatchQuery("author", "rowling"),
                        new MatchQuery("title", "prince")),
                                 ClauseType.MUST),
                10));
        System.out.println(ResultUtil.getBookRepresentation(result));
        Assertions.assertEquals(2, result.getMatchingResults().size());
    }

    @Test
    void testShouldClauseSearch() throws ForageSearchError {
        ForageQueryResult result = searchEngine.query(new ForageSearchQuery(
                new BooleanQuery(Lists.newArrayList(
                        new MatchQuery("author", "rowling"),
                        new MatchQuery("title", "prince")),
                                 ClauseType.SHOULD),
                10));

        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertTrue(10 < result.getTotal().getTotal());
    }

    @Test
    void testIntRangeSearch() throws ForageSearchError {
        ForageQueryResult result;

        result = searchEngine.query(new ForageSearchQuery(
                new RangeQuery("numPage", new IntRange(600, 800)),
                10));
        Assertions.assertTrue(result.getMatchingResults()
                                      .stream()
                                      .map(matchingResult -> ((Book) matchingResult.getData()).getNumPage())
                                      .allMatch(pages -> pages <= 800 && pages >= 600));
        System.out.println("result.getTotal().getTotal() = " + result.getTotal().getTotal());
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(644, result.getTotal().getTotal());


        result = searchEngine.query(new ForageSearchQuery(
                new RangeQuery("numPage", new IntRange(100, 200)), 10));
        Assertions.assertTrue(result.getMatchingResults()
                                      .stream()
                                      .map(matchingResult -> ((Book) matchingResult.getData()).getNumPage())
                                      .allMatch(pages -> pages >= 100 && pages <= 200));
        System.out.println("result.getTotal().getTotal() = " + result.getTotal().getTotal());
        Assertions.assertEquals(10, result.getMatchingResults().size());
        Assertions.assertEquals(1001, result.getTotal().getTotal());
    }
}
