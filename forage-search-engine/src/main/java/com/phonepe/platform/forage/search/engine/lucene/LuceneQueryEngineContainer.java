package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.core.ItemConsumer;
import com.phonepe.platform.forage.models.result.ForageQueryResult;
import com.phonepe.platform.forage.search.engine.QueryEngine;
import com.phonepe.platform.forage.search.engine.core.IndexingConsumer;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.index.IndexableDocument;
import com.phonepe.platform.forage.models.query.ForageQuery;

import java.util.function.Supplier;

public class LuceneQueryEngineContainer<T>
        implements QueryEngine<ForageQuery, ForageQueryResult<T>>, Supplier<LuceneSearchEngine<T>>,
                   ItemConsumer<IndexableDocument<T>> {

    private final LuceneSearchEngineBuilder<T> builder;
    private final IndexingConsumer<T> indexingConsumer;

    public LuceneQueryEngineContainer(final LuceneSearchEngineBuilder<T> builder) {
        this.builder = builder;
        this.indexingConsumer = new IndexingConsumer<>(this);
    }

    @Override
    public ForageQueryResult<T> query(final ForageQuery query) throws ForageSearchError {
        if (indexingConsumer.searchEngine() == null) {
            throw new ForageSearchError(ForageErrorCode.QUERY_ENGINE_NOT_INITIALIZED_YET, "Engine not ready for query");
        }
        return indexingConsumer.searchEngine().query(query);
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
        indexingConsumer.init();
    }

    @Override
    public void consume(final IndexableDocument<T> tIndexableDocument) throws Exception {
        indexingConsumer.consume(tIndexableDocument);
    }

    @Override
    public void finish() throws Exception {
        indexingConsumer.finish();
    }
}
