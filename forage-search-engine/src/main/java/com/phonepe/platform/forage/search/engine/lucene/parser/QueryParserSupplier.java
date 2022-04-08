package com.phonepe.platform.forage.search.engine.lucene.parser;

import org.apache.lucene.queryparser.classic.QueryParser;

public interface QueryParserSupplier {
    QueryParser queryParser(String field);
}
