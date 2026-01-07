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

package com.livetheoogway.forage.models.query.util;

import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.ForageSearchQuery;
import com.livetheoogway.forage.models.query.SortCriteria;
import com.livetheoogway.forage.models.query.SortOrder;
import com.livetheoogway.forage.models.query.search.*;
import com.livetheoogway.forage.models.query.search.score.ConstantScoreFunction;
import com.livetheoogway.forage.models.query.search.score.FieldValueFactorFunction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderBoostTest {

    @Test
    void testMatchQueryBuilderWithBoost() {
        Query query = QueryBuilder.matchQuery("title", "java").boost(2.0f).build();
        
        assertInstanceOf(MatchQuery.class, query);
        MatchQuery matchQuery = (MatchQuery) query;
        assertEquals("title", matchQuery.getField());
        assertEquals("java", matchQuery.getValue());
        assertEquals(2.0f, matchQuery.getBoost());
    }

    @Test
    void testMatchQueryBuilderWithoutBoost() {
        Query query = QueryBuilder.matchQuery("title", "java").build();
        
        assertInstanceOf(MatchQuery.class, query);
        MatchQuery matchQuery = (MatchQuery) query;
        assertEquals("title", matchQuery.getField());
        assertEquals("java", matchQuery.getValue());
        assertNull(matchQuery.getBoost());
    }

    @Test
    void testFuzzyMatchQueryBuilderWithBoost() {
        Query query = QueryBuilder.fuzzyMatchQuery("title", "jva").boost(1.5f).build();
        
        assertInstanceOf(FuzzyMatchQuery.class, query);
        FuzzyMatchQuery fuzzyQuery = (FuzzyMatchQuery) query;
        assertEquals("title", fuzzyQuery.getField());
        assertEquals("jva", fuzzyQuery.getValue());
        assertEquals(1.5f, fuzzyQuery.getBoost());
    }

    @Test
    void testPhraseMatchQueryBuilderWithBoost() {
        Query query = QueryBuilder.phraseMatchQuery("title", "machine learning").boost(2.5f).build();
        
        assertInstanceOf(PhraseMatchQuery.class, query);
        PhraseMatchQuery phraseQuery = (PhraseMatchQuery) query;
        assertEquals("title", phraseQuery.getField());
        assertEquals("machine learning", phraseQuery.getPhrase());
        assertEquals(2.5f, phraseQuery.getBoost());
    }

    @Test
    void testPrefixMatchQueryBuilderWithBoost() {
        Query query = QueryBuilder.prefixMatchQuery("author", "smi").boost(1.2f).build();
        
        assertInstanceOf(PrefixMatchQuery.class, query);
        PrefixMatchQuery prefixQuery = (PrefixMatchQuery) query;
        assertEquals("author", prefixQuery.getField());
        assertEquals("smi", prefixQuery.getValue());
        assertEquals(1.2f, prefixQuery.getBoost());
    }

    @Test
    void testIntRangeQueryBuilderWithBoost() {
        Query query = QueryBuilder.intRangeQuery("pages", 100, 500).boost(0.8f).build();
        
        assertInstanceOf(RangeQuery.class, query);
        RangeQuery rangeQuery = (RangeQuery) query;
        assertEquals("pages", rangeQuery.getField());
        assertEquals(0.8f, rangeQuery.getBoost());
    }

    @Test
    void testFloatRangeQueryBuilderWithBoost() {
        Query query = QueryBuilder.floatRangeQuery("rating", 3.0f, 5.0f).boost(1.8f).build();
        
        assertInstanceOf(RangeQuery.class, query);
        RangeQuery rangeQuery = (RangeQuery) query;
        assertEquals("rating", rangeQuery.getField());
        assertEquals(1.8f, rangeQuery.getBoost());
    }

    @Test
    void testMatchAllQueryBuilderWithBoost() {
        Query query = QueryBuilder.matchAllQuery().boost(0.5f).build();
        
        assertInstanceOf(MatchAllQuery.class, query);
        MatchAllQuery matchAllQuery = (MatchAllQuery) query;
        assertEquals(0.5f, matchAllQuery.getBoost());
    }

    @Test
    void testBooleanQueryBuilderWithBoost() {
        Query query = QueryBuilder.booleanQuery()
                .query(new MatchQuery("title", "java"))
                .query(new MatchQuery("author", "gosling"))
                .clauseType(ClauseType.MUST)
                .boost(1.5f)
                .build();
        
        assertInstanceOf(BooleanQuery.class, query);
        BooleanQuery booleanQuery = (BooleanQuery) query;
        assertEquals(2, booleanQuery.getQueries().size());
        assertEquals(ClauseType.MUST, booleanQuery.getClauseType());
        assertEquals(1.5f, booleanQuery.getBoost());
    }

    @Test
    void testFunctionScoreQueryBuilder() {
        Query baseQuery = QueryBuilder.matchQuery("category", "programming").build();
        Query query = QueryBuilder.functionScoreQuery()
                .baseQuery(baseQuery)
                .constantScore(2.0f)
                .boost(1.5f)
                .build();
        
        assertInstanceOf(FunctionScoreQuery.class, query);
        FunctionScoreQuery functionQuery = (FunctionScoreQuery) query;
        assertEquals(baseQuery, functionQuery.getBaseQuery());
        assertEquals(1.5f, functionQuery.getBoost());
        assertInstanceOf(ConstantScoreFunction.class, functionQuery.getScoreFunction());
        
        ConstantScoreFunction scoreFunction = (ConstantScoreFunction) functionQuery.getScoreFunction();
        assertEquals(2.0f, scoreFunction.getValue());
    }

    @Test
    void testFunctionScoreQueryBuilderWithFieldValueFactor() {
        Query baseQuery = QueryBuilder.matchQuery("title", "java").build();
        Query query = QueryBuilder.functionScoreQuery()
                .baseQuery(baseQuery)
                .fieldValueFactor("popularity", 1.5f)
                .build();
        
        assertInstanceOf(FunctionScoreQuery.class, query);
        FunctionScoreQuery functionQuery = (FunctionScoreQuery) query;
        assertEquals(baseQuery, functionQuery.getBaseQuery());
        assertNull(functionQuery.getBoost());
        assertInstanceOf(FieldValueFactorFunction.class, functionQuery.getScoreFunction());
        
        FieldValueFactorFunction scoreFunction = (FieldValueFactorFunction) functionQuery.getScoreFunction();
        assertEquals("popularity", scoreFunction.getField());
        assertEquals(1.5f, scoreFunction.getFactor());
    }

    @Test
    void testForageQueryWithSortingAndMinimumScore() {
        List<SortCriteria> sortBy = Arrays.asList(
            SortCriteria.byScore(SortOrder.DESC),
            new SortCriteria("rating", SortOrder.DESC)
        );
        
        ForageQuery forageQuery = QueryBuilder.matchQuery("title", "java")
                .buildForageQuery(10, sortBy, 0.5f);
        
        assertInstanceOf(ForageSearchQuery.class, forageQuery);
        ForageSearchQuery searchQuery = (ForageSearchQuery) forageQuery;
        assertEquals(10, searchQuery.getSize());
        assertEquals(sortBy, searchQuery.getSortBy());
        assertEquals(0.5f, searchQuery.getMinimumScore());
    }

    @Test
    void testForageQueryWithSorting() {
        List<SortCriteria> sortBy = Arrays.asList(
            new SortCriteria("rating", SortOrder.DESC)
        );
        
        ForageQuery forageQuery = QueryBuilder.matchQuery("author", "martin")
                .buildForageQuery(15, sortBy);
        
        assertInstanceOf(ForageSearchQuery.class, forageQuery);
        ForageSearchQuery searchQuery = (ForageSearchQuery) forageQuery;
        assertEquals(15, searchQuery.getSize());
        assertEquals(sortBy, searchQuery.getSortBy());
        assertNull(searchQuery.getMinimumScore());
    }

    @Test
    void testFunctionScoreQueryBuilderValidation() {
        // Test that builder throws exception when base query is missing
        assertThrows(IllegalStateException.class, () -> 
            QueryBuilder.functionScoreQuery()
                .constantScore(2.0f)
                .build()
        );

        // Test that builder throws exception when score function is missing
        Query baseQuery = QueryBuilder.matchQuery("title", "java").build();
        assertThrows(IllegalStateException.class, () -> 
            QueryBuilder.functionScoreQuery()
                .baseQuery(baseQuery)
                .build()
        );
    }

    @Test
    void testBuilderMethodChaining() {
        // Test that all builder methods return the builder for chaining
        Query query = QueryBuilder.matchQuery("title", "test")
                .boost(2.0f)
                .build();
        
        assertInstanceOf(MatchQuery.class, query);
        assertEquals(2.0f, ((MatchQuery) query).getBoost());
    }

    @Test
    void testForageQueryBuilderWithMinimumScoreOnly() {
        ForageQuery forageQuery = QueryBuilder.matchQuery("title", "java")
                .buildForageQuery(10, null, 0.3f);
        
        assertInstanceOf(ForageSearchQuery.class, forageQuery);
        ForageSearchQuery searchQuery = (ForageSearchQuery) forageQuery;
        assertEquals(10, searchQuery.getSize());
        assertNull(searchQuery.getSortBy());
        assertEquals(0.3f, searchQuery.getMinimumScore());
    }

    @Test
    void testForageQueryBuilderWithDefaultsOnly() {
        ForageQuery forageQuery = QueryBuilder.matchQuery("title", "java")
                .buildForageQuery(5);
        
        assertInstanceOf(ForageSearchQuery.class, forageQuery);
        ForageSearchQuery searchQuery = (ForageSearchQuery) forageQuery;
        assertEquals(5, searchQuery.getSize());
        assertNull(searchQuery.getSortBy());
        assertNull(searchQuery.getMinimumScore());
    }
}