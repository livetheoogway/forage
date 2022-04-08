package com.phonepe.platform.forage.search.engine.lucene.parser;

import lombok.AllArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

@AllArgsConstructor
public class QueryParserFactory implements QueryParserSupplier {
    private final Analyzer analyzer;

    @Override
    public QueryParser queryParser(final String field) {
        return new QueryParser(field, analyzer);
    }
}
