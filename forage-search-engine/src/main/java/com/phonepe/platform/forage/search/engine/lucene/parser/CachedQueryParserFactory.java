package com.phonepe.platform.forage.search.engine.lucene.parser;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

public class CachedQueryParserFactory extends QueryParserFactory {
    private final LoadingCache<String, QueryParser> cache;

    public CachedQueryParserFactory(final Analyzer analyzer, int maxSize) {
        super(analyzer);
        cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .build(super::queryParser);
    }

    @Override
    public QueryParser queryParser(final String field) {
        return cache.get(field);
    }
}
