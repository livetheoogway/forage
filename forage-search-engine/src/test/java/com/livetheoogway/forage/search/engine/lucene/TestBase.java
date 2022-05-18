package com.livetheoogway.forage.search.engine.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

class TestBase {
    @Test
    void testQueryParserLogic() throws ParseException {
        String s ="author:rowling";
        final QueryParser queryParser = new QueryParser("temp", new StandardAnalyzer());
        final Query parse = queryParser.parse(s);
        System.out.println("parse = " + parse);
    }
}
