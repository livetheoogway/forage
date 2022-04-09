package com.phonepe.platform.forage.search.engine.lucene.parser;

import com.phonepe.platform.forage.search.engine.util.MaxSizeHashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.Map;

public class CachedQueryParserFactory extends QueryParserFactory {
    private final Map<String, QueryParser> cache;

    public CachedQueryParserFactory(final Analyzer analyzer, int maxSize) {
        super(analyzer);
        cache = new MaxSizeHashMap<>(maxSize);
    }

    @Override
    public QueryParser queryParser(final String field) {
        cache.computeIfAbsent(field, super::queryParser);
        return cache.get(field);
    }
}
