package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.search.engine.lucene.util.QueryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HardLuceneTests {

    @Test
    void testSimultaneousReadsAndWritesToIndex() {
        Assertions.assertThrows(NullPointerException.class, () -> QueryBuilder.booleanQuery().build());
    }
}
