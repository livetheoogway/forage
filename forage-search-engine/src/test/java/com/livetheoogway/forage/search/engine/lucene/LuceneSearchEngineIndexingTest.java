package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.models.result.field.TextField;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.util.QueryBuilder;
import com.livetheoogway.forage.search.engine.ResultUtil;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
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
                = searchEngine.query(QueryBuilder.matchQuery("pod", "nexus").build());
        Assertions.assertEquals(1, result.getMatchingResults().size());
        final String representation = ResultUtil.getRepresentation(result, MatchingResult::getData,
                                                                   (a, b) -> a + "\n" + b);
        Assertions.assertEquals("Some nexus object", representation);
    }
}