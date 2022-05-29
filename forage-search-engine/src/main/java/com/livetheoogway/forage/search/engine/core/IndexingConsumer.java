package com.livetheoogway.forage.search.engine.core;

import com.livetheoogway.forage.core.ItemConsumer;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.ForageLuceneSearchEngine;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
