package com.phonepe.platform.forage.search.engine.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.lucene.parser.CachedQueryParserFactory;
import com.phonepe.platform.forage.search.engine.lucene.parser.QueryParserFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class LuceneSearchEngineBuilder {
    private static final int DEFAULT_FIELD_SIZE_SUGGESTION = 10;

    private ObjectMapper mapper;
    private Analyzer analyzer;
    private int maxFieldSizeHint;
    private QueryParserFactory queryParserFactory;

    public LuceneSearchEngineBuilder withMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public LuceneSearchEngineBuilder withAnalyser(final Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public LuceneSearchEngineBuilder withQueryParserFactory(final QueryParserFactory queryParserFactory) {
        this.queryParserFactory = queryParserFactory;
        return this;
    }

    public LuceneSearchEngineBuilder withMaxFieldSizeHint(final int maxFieldSizeHint) {
        this.maxFieldSizeHint = maxFieldSizeHint;
        return this;
    }

    public static LuceneSearchEngineBuilder builder() {
        return new LuceneSearchEngineBuilder();
    }

    public LuceneSearchEngine build() throws ForageSearchError {
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
        return new LuceneSearchEngine(mapper, queryParserFactory, analyzer);
    }
}
