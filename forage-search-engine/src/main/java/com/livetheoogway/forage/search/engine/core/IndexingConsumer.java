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

package com.livetheoogway.forage.search.engine.core;

import com.livetheoogway.forage.core.ItemConsumer;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.ForageLuceneSearchEngine;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * This class implements the core libraries {@link ItemConsumer}.
 * It is responsible for swapping the search engine during every bootstrap.
 * Here, we keep a reference of the current search engine using an {@link AtomicReference}.
 * During every bootstrap, we initialize a new search engine, and once all items are consumed, we flush (lucene flush
 * makes the documents searchable), and then swap the newly created engine, with the current one, so that it can
 * serve all new search requests
 *
 * @param <T> Type of data being stored in the main database
 */
public class IndexingConsumer<T> implements ItemConsumer<IndexableDocument> {

    private final Supplier<ForageLuceneSearchEngine<T>> newSearchEngineSupplier;
    private final AtomicReference<ForageLuceneSearchEngine<T>> currentReference;
    private ForageLuceneSearchEngine<T> engine;

    public IndexingConsumer(final Supplier<ForageLuceneSearchEngine<T>> newSearchEngineSupplier) {
        this.newSearchEngineSupplier = newSearchEngineSupplier;
        this.currentReference = new AtomicReference<>();
    }

    @Override
    public void init() throws Exception {
        if (engine == null) {
            engine = newSearchEngineSupplier.get();
        } else {
            throw ForageSearchError.raise(ForageErrorCode.INVALID_STATE,
                                          "Update listeners init is being called twice. This should not be happening");
        }
    }

    @Override
    public void consume(final IndexableDocument indexableDocument) throws Exception {
        engine.index(indexableDocument);
    }

    @Override
    public void finish() throws IOException, ForageSearchError {
        synchronized (this) {
            try (ForageLuceneSearchEngine<T> ignored = currentReference.get()) {
                engine.flush();
                currentReference.set(engine);
            } finally {
                engine = null;
            }
        }
    }

    public ForageLuceneSearchEngine<T> searchEngine() {
        return currentReference.get();
    }
}
