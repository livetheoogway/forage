package com.livetheoogway.forage.search.engine.lucene.parser;

import org.apache.lucene.queryparser.classic.QueryParser;

public interface QueryParserSupplier {
    QueryParser queryParser(String field);
}
