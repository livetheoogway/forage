package com.livetheoogway.forage.search.engine.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.lucene.parser.CachedQueryParserFactory;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserFactory;
import com.livetheoogway.forage.search.engine.store.Store;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class LuceneSearchEngineBuilder<T> {
    private static final int DEFAULT_FIELD_SIZE_SUGGESTION = 10;

    private ObjectMapper mapper;
    private Analyzer analyzer;
    private int maxFieldSizeHint;
    private QueryParserFactory queryParserFactory;
    private Store<T> store;

    public LuceneSearchEngineBuilder<T> withObjectMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public LuceneSearchEngineBuilder<T> withAnalyser(final Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public LuceneSearchEngineBuilder<T> withQueryParserFactory(final QueryParserFactory queryParserFactory) {
        this.queryParserFactory = queryParserFactory;
        return this;
    }

    public LuceneSearchEngineBuilder<T> withMaxFieldSizeHint(final int maxFieldSizeHint) {
        this.maxFieldSizeHint = maxFieldSizeHint;
        return this;
    }

    public LuceneSearchEngineBuilder<T> withDataStore(final Store<T> store) {
        this.store = store;
        return this;
    }

    public static <T> LuceneSearchEngineBuilder<T> builder() {
        return new LuceneSearchEngineBuilder<>();
    }

    public LuceneSearchEngine<T> build() throws ForageSearchError {
        /* validations */
        if (this.store == null) {
            throw ForageSearchError.raise(ForageErrorCode.DATASTORE_INVALID, "no datastore passed to builder");
        }

        /* defaults */
        if (this.analyzer == null) {
            this.analyzer = new StandardAnalyzer();
        }
        if (this.maxFieldSizeHint < 1) {
            this.maxFieldSizeHint = DEFAULT_FIELD_SIZE_SUGGESTION;
        }
        if (queryParserFactory == null) {
            queryParserFactory = new CachedQueryParserFactory(analyzer, maxFieldSizeHint);
        }
        if (mapper == null) {
            this.mapper = new ObjectMapper();
        }

        /* create a new engine */
        return new LuceneSearchEngine<>(mapper, queryParserFactory, store, analyzer);
    }
}
