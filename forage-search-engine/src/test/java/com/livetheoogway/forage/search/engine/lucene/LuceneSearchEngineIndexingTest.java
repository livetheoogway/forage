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

import com.livetheoogway.forage.models.DataId;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.field.TextField;
import com.livetheoogway.forage.search.engine.ResultUtil;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LuceneSearchEngineIndexingTest {

    private ForageLuceneSearchEngine<SomeObject> searchEngine;
    private InMemoryHashStore<SomeObject> store;

    @BeforeEach
    void setUp() throws ForageSearchError {
        store = new InMemoryHashStore<>();
        searchEngine = ForageSearchEngineBuilder.<SomeObject>builder()
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
                = searchEngine.search(QueryBuilder.matchQuery("pod", "nexus").buildForageQuery());
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