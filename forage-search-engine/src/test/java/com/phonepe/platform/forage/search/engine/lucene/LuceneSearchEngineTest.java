package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.search.engine.ForageSearchEngine;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.ForageDocument;
import com.phonepe.platform.forage.search.engine.model.IndexableDocument;
import com.phonepe.platform.forage.search.engine.model.field.TextField;
import com.phonepe.platform.forage.search.engine.model.query.ForageQuery;
import com.phonepe.platform.forage.search.engine.model.query.ForageSearchQuery;
import com.phonepe.platform.forage.search.engine.model.query.result.ForageQueryResult;
import com.phonepe.platform.forage.search.engine.model.query.search.IsQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class LuceneSearchEngineTest {

    private ForageSearchEngine<IndexableDocument, ForageQuery, ForageQueryResult> searchEngine;

    @BeforeEach
    void setUp() throws IOException, ForageSearchError {
        searchEngine = LuceneSearchEngineBuilder.builder().build();
    }

    @Test
    void simpleIndexedSearch() throws ForageSearchError {
        searchEngine.index(ForageDocument.builder()
                                   .id("ID1")
                                   .field(new TextField("pod", "nexus"))
                                   .field(new TextField("app", "android"))
                                   .data("Some nexus object")
                                   .build());
        searchEngine.flush();

        final ForageQueryResult result
                = searchEngine.query(new ForageSearchQuery(new IsQuery("pod", "nexus"), 10));
        System.out.println(result);
    }
}