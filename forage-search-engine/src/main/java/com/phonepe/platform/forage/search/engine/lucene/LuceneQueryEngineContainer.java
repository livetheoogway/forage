package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.core.UpdateListener;
import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.search.engine.QueryEngine;
import com.phonepe.platform.forage.search.engine.core.IndexingUpdateListener;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;
import com.phonepe.platform.forage.search.engine.model.query.ForageQuery;

import java.util.function.Supplier;

public class LuceneQueryEngineContainer<T>
        implements QueryEngine<ForageQuery, ForageQueryResult<T>>, Supplier<LuceneSearchEngine<T>>,
                   UpdateListener<IndexableDocument<T>> {

    private final LuceneSearchEngineBuilder<T> builder;
    private final IndexingUpdateListener<T> indexingUpdateListener;

    public LuceneQueryEngineContainer(final LuceneSearchEngineBuilder<T> builder) {
        this.builder = builder;
        this.indexingUpdateListener = new IndexingUpdateListener<>(this);
    }

    @Override
    public ForageQueryResult<T> query(final ForageQuery query) throws ForageSearchError {
        if (indexingUpdateListener.getCurrentReference().get() == null) {
            throw new ForageSearchError(ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET, "Engine not ready for query");
        }
        return indexingUpdateListener.getCurrentReference().get().query(query);
    }

    @Override
    public LuceneSearchEngine<T> get() {
        try {
            return builder.build(); //todo
        } catch (ForageSearchError e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void init() throws Exception {
        indexingUpdateListener.init();
    }

    @Override
    public void takeUpdate(final IndexableDocument<T> tIndexableDocument) throws Exception {
        indexingUpdateListener.takeUpdate(tIndexableDocument);
    }

    @Override
    public void finish() throws Exception {
        indexingUpdateListener.finish();
    }
}
