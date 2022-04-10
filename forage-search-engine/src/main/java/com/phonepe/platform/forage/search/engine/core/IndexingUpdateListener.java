package com.phonepe.platform.forage.search.engine.core;

import com.phonepe.platform.forage.core.UpdateListener;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.lucene.LuceneSearchEngine;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class IndexingUpdateListener<T> implements UpdateListener<IndexableDocument<T>> {

    private final Supplier<LuceneSearchEngine<T>> newSearchEngineSupplier;
    @Getter
    private final AtomicReference<LuceneSearchEngine<T>> currentReference;
    private LuceneSearchEngine<T> engine;

    public IndexingUpdateListener(final Supplier<LuceneSearchEngine<T>> newSearchEngineSupplier) {
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
    public void takeUpdate(final IndexableDocument<T> indexableDocument) throws Exception {
        engine.index(indexableDocument);
    }

    @Override
    public void finish() throws Exception {
        synchronized (this) {
            LuceneSearchEngine<T> olderEngine = currentReference.get();
            engine.flush();
            currentReference.set(engine);
            engine = null;
            if (olderEngine != null) {
                olderEngine.close();
            }
        }
    }
}
