package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.DataId;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.field.TextField;
import com.livetheoogway.forage.search.engine.ResultUtil;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.util.QueryBuilder;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LuceneSearchEngineIndexingTest {

    private LuceneSearchEngine<SomeObject> searchEngine;
    private InMemoryHashStore<SomeObject> store;

    @BeforeEach
    void setUp() throws ForageSearchError {
        store = new InMemoryHashStore<>();
        searchEngine = LuceneSearchEngineBuilder.<SomeObject>builder()
                .withDataStore(store).build();
    }

    @Test
    void simpleIndexedSearch() throws ForageSearchError {
        store.store(new SomeObject("Some data"));
        searchEngine.index(ForageDocument.<String>builder()
                                   .id("ID1")
                                   .field(new TextField("pod", "nexus"))
                                   .field(new TextField("app", "android"))
                                   .build());
        searchEngine.flush();


        final ForageQueryResult<SomeObject> result
                = searchEngine.query(QueryBuilder.matchQuery("pod", "nexus").build());
        Assertions.assertEquals(1, result.getMatchingResults().size());
        final String representation = ResultUtil.getRepresentation(result,
                                                                   data -> data.getData().getData(),
                                                                   (a, b) -> a + "\n" + b);
        Assertions.assertEquals("Some data", representation);
    }


    @AllArgsConstructor
    private static class SomeObject implements DataId {
        @Getter
        private String data;

        @Override
        public String id() {
            return "ID1";
        }
    }
}