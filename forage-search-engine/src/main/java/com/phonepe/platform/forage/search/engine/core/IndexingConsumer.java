package com.phonepe.platform.forage.search.engine.core;

import com.phonepe.platform.forage.core.ItemConsumer;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.lucene.LuceneSearchEngine;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class IndexingConsumer<T> implements ItemConsumer<IndexableDocument<T>> {

    private final Supplier<LuceneSearchEngine<T>> newSearchEngineSupplier;
    private final AtomicReference<LuceneSearchEngine<T>> currentReference;
    private LuceneSearchEngine<T> engine;

    public IndexingConsumer(final Supplier<LuceneSearchEngine<T>> newSearchEngineSupplier) {
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
    public void consume(final IndexableDocument<T> indexableDocument) throws Exception {
        engine.index(indexableDocument);
    }

    @Override
    public void finish() throws IOException, ForageSearchError {
        synchronized (this) {
            LuceneSearchEngine<T> olderEngine = currentReference.get();
            try {
                engine.flush();
                currentReference.set(engine);
            } finally {
                engine = null;
                if (olderEngine != null) {
                    olderEngine.close();
                }
            }
        }
    }

    public LuceneSearchEngine<T> searchEngine() {
        return currentReference.get();
    }
}
