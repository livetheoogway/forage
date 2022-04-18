package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.models.result.MatchingResult;
import com.phonepe.platform.forage.models.result.field.TextField;
import com.phonepe.platform.forage.search.engine.ResultUtil;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.index.ForageDocument;
import com.phonepe.platform.forage.search.engine.model.query.ForageSearchQuery;
import com.phonepe.platform.forage.search.engine.model.query.search.MatchQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LuceneSearchEngineIndexingTest {

    private LuceneSearchEngine<String> searchEngine;

    @BeforeEach
    void setUp() throws ForageSearchError {
        searchEngine = LuceneSearchEngineBuilder.<String>builder().build();
    }

    @Test
    void simpleIndexedSearch() throws ForageSearchError {
        searchEngine.index(ForageDocument.<String>builder()
                                   .id("ID1")
                                   .field(new TextField("pod", "nexus"))
                                   .field(new TextField("app", "android"))
                                   .data("Some nexus object")
                                   .build());
        searchEngine.flush();

        final ForageQueryResult<String> result
                = searchEngine.query(new ForageSearchQuery(new MatchQuery("pod", "nexus"), 10));
        Assertions.assertEquals(1, result.getMatchingResults().size());
        final String representation = ResultUtil.getRepresentation(result, MatchingResult::getData,
                                                                   (a, b) -> a + "\n" + b);
        Assertions.assertEquals("Some nexus object", representation);
    }
}