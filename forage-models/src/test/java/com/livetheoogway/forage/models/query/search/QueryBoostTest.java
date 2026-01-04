/*
 * Copyright 2026. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.models.query.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryBoostTest {

    @Test
    void testMatchQueryWithBoost() {
        MatchQuery query = new MatchQuery("title", "java", 2.0f);
        
        assertEquals("title", query.getField());
        assertEquals("java", query.getValue());
        assertEquals(2.0f, query.getBoost());
        assertEquals(QueryType.MATCH, query.getType());
    }

    @Test
    void testMatchQueryWithoutBoost() {
        MatchQuery query = new MatchQuery("title", "java");
        
        assertEquals("title", query.getField());
        assertEquals("java", query.getValue());
        assertNull(query.getBoost());
        assertEquals(QueryType.MATCH, query.getType());
    }

    @Test
    void testFuzzyMatchQueryWithBoost() {
        FuzzyMatchQuery query = new FuzzyMatchQuery("title", "jva", 1.5f);
        
        assertEquals("title", query.getField());
        assertEquals("jva", query.getValue());
        assertEquals(1.5f, query.getBoost());
        assertEquals(QueryType.FUZZY_MATCH, query.getType());
    }

    @Test
    void testFuzzyMatchQueryWithoutBoost() {
        FuzzyMatchQuery query = new FuzzyMatchQuery("title", "jva");
        
        assertEquals("title", query.getField());
        assertEquals("jva", query.getValue());
        assertNull(query.getBoost());
        assertEquals(QueryType.FUZZY_MATCH, query.getType());
    }

    @Test
    void testPhraseMatchQueryWithBoost() {
        PhraseMatchQuery query = new PhraseMatchQuery("title", "machine learning", 2.5f);
        
        assertEquals("title", query.getField());
        assertEquals("machine learning", query.getPhrase());
        assertEquals(2.5f, query.getBoost());
        assertEquals(QueryType.PHRASE, query.getType());
    }

    @Test
    void testPhraseMatchQueryWithoutBoost() {
        PhraseMatchQuery query = new PhraseMatchQuery("title", "machine learning");
        
        assertEquals("title", query.getField());
        assertEquals("machine learning", query.getPhrase());
        assertNull(query.getBoost());
        assertEquals(QueryType.PHRASE, query.getType());
    }

    @Test
    void testPrefixMatchQueryWithBoost() {
        PrefixMatchQuery query = new PrefixMatchQuery("author", "smi", 1.2f);
        
        assertEquals("author", query.getField());
        assertEquals("smi", query.getValue());
        assertEquals(1.2f, query.getBoost());
        assertEquals(QueryType.PREFIX_MATCH, query.getType());
    }

    @Test
    void testPrefixMatchQueryWithoutBoost() {
        PrefixMatchQuery query = new PrefixMatchQuery("author", "smi");
        
        assertEquals("author", query.getField());
        assertEquals("smi", query.getValue());
        assertNull(query.getBoost());
        assertEquals(QueryType.PREFIX_MATCH, query.getType());
    }

    @Test
    void testMatchAllQueryWithBoost() {
        MatchAllQuery query = new MatchAllQuery(3.0f);
        
        assertEquals(3.0f, query.getBoost());
        assertEquals(QueryType.MATCH_ALL, query.getType());
    }

    @Test
    void testMatchAllQueryWithoutBoost() {
        MatchAllQuery query = new MatchAllQuery();
        
        assertNull(query.getBoost());
        assertEquals(QueryType.MATCH_ALL, query.getType());
    }

    @Test
    void testBooleanQueryWithBoost() {
        MatchQuery subQuery1 = new MatchQuery("title", "java");
        MatchQuery subQuery2 = new MatchQuery("author", "gosling");
        
        BooleanQuery query = new BooleanQuery(
            java.util.Arrays.asList(subQuery1, subQuery2),
            ClauseType.MUST,
            2.0f
        );
        
        assertEquals(2, query.getQueries().size());
        assertEquals(ClauseType.MUST, query.getClauseType());
        assertEquals(2.0f, query.getBoost());
        assertEquals(QueryType.BOOLEAN, query.getType());
    }

    @Test
    void testBooleanQueryWithoutBoost() {
        MatchQuery subQuery = new MatchQuery("title", "java");
        
        BooleanQuery query = new BooleanQuery(
            java.util.Arrays.asList(subQuery),
            ClauseType.SHOULD
        );
        
        assertEquals(1, query.getQueries().size());
        assertEquals(ClauseType.SHOULD, query.getClauseType());
        assertNull(query.getBoost());
        assertEquals(QueryType.BOOLEAN, query.getType());
    }

    @Test
    void testRangeQueryWithBoost() {
        RangeQuery query = new RangeQuery("pages", null, 1.8f);
        
        assertEquals("pages", query.getField());
        assertEquals(1.8f, query.getBoost());
        assertEquals(QueryType.RANGE, query.getType());
    }

    @Test
    void testRangeQueryWithoutBoost() {
        RangeQuery query = new RangeQuery("pages", null);
        
        assertEquals("pages", query.getField());
        assertNull(query.getBoost());
        assertEquals(QueryType.RANGE, query.getType());
    }
}