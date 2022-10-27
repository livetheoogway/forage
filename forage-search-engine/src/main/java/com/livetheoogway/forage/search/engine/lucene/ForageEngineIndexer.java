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

import com.livetheoogway.forage.core.ItemConsumer;
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.search.engine.ForageSearchEngine;
import com.livetheoogway.forage.search.engine.core.SearchEngineSwapReferenceHandler;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class ForageEngineIndexer<T>
        implements ForageSearchEngine<T>, Supplier<ForageLuceneSearchEngine<T>>,
                   ItemConsumer<IndexableDocument> {

    private final ForageSearchEngineBuilder<T> builder;
    private final SearchEngineSwapReferenceHandler<IndexableDocument, ForageLuceneSearchEngine<T>> searchEngineSwapReferenceHandler;

    public ForageEngineIndexer(final ForageSearchEngineBuilder<T> builder) {
        this.builder = builder;
        this.searchEngineSwapReferenceHandler = new SearchEngineSwapReferenceHandler<>(this);
    }

    @Override
    public ForageQueryResult<T> search(final ForageQuery query) throws ForageSearchError {
        if (searchEngineSwapReferenceHandler.searchEngine() == null) {
            throw new ForageSearchError(ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET, "Engine not ready for query");
        }
        return searchEngineSwapReferenceHandler.searchEngine().search(query);
    }

    @Override
    @SneakyThrows
    public ForageLuceneSearchEngine<T> get() {
        return builder.build();
    }

    @Override
    public void init() throws Exception {
        searchEngineSwapReferenceHandler.init();
    }

    @Override
    public void consume(final IndexableDocument indexableDocument) throws Exception {
        searchEngineSwapReferenceHandler.consume(indexableDocument);
    }

    @Override
    public void finish() throws Exception {
        searchEngineSwapReferenceHandler.finish();
    }
}
