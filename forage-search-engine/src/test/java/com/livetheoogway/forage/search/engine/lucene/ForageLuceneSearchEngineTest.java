package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.TestUtils;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForageLuceneSearchEngineTest {

    @Test
    void testThatThereAreNoErrorsWhenThereIsNothingToIndex() throws ForageSearchError {
        ForageLuceneSearchEngine<Book> searchEngine;
        InMemoryHashStore<Book> dataStore = new InMemoryHashStore<>();
        searchEngine = ForageSearchEngineBuilder.<Book>builder()
                .withObjectMapper(TestUtils.mapper())
                .withDataStore(dataStore).build();
        searchEngine.flush();
        final ForageQueryResult<Book> search = searchEngine.search(QueryBuilder.matchAllQuery().buildForageQuery());
        Assertions.assertEquals(0, search.getTotal().getTotal());
    }
}