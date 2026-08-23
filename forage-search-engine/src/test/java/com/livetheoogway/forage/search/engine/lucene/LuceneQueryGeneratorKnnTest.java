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

package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.models.query.search.KnnQuery;
import com.livetheoogway.forage.models.query.search.MatchQuery;
import com.livetheoogway.forage.search.engine.lucene.parser.QueryParserSupplier;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Unit tests for the {@link LuceneQueryGenerator#visit(KnnQuery)} method, covering translation
 * of the domain {@link KnnQuery} model into Lucene's {@link KnnFloatVectorQuery}, including
 * boost application and hybrid-search filter conversion.
 */
class LuceneQueryGeneratorKnnTest {

    private LuceneQueryGenerator generator;

    @BeforeEach
    void setUp() {
        Analyzer analyzer = new StandardAnalyzer();
        QueryParserSupplier supplier = field -> new QueryParser(field, analyzer);
        generator = new LuceneQueryGenerator(analyzer, supplier);
    }

    @Test
    void knnQueryWithoutBoostOrFilterProducesPlainKnnFloatVectorQuery() throws Exception {
        float[] vector = {1.0f, 0.5f, 0.0f};
        KnnQuery knnQuery = new KnnQuery("embedding", vector, 5);

        Query result = generator.visit(knnQuery);

        assertInstanceOf(KnnFloatVectorQuery.class, result);
        assertEquals(new KnnFloatVectorQuery("embedding", vector, 5, null), result);
    }

    @Test
    void knnQueryWithBoostWrapsResultInBoostQuery() throws Exception {
        float[] vector = {1.0f, 0.0f};
        KnnQuery knnQuery = new KnnQuery("embedding", vector, 3, 2.5f, null);

        Query result = generator.visit(knnQuery);

        assertInstanceOf(BoostQuery.class, result);
        BoostQuery boostQuery = (BoostQuery) result;
        assertEquals(2.5f, boostQuery.getBoost());
        assertEquals(new KnnFloatVectorQuery("embedding", vector, 3, null), boostQuery.getQuery());
    }

    @Test
    void knnQueryWithBoostOfOneDoesNotWrapInBoostQuery() throws Exception {
        float[] vector = {1.0f, 0.0f};
        KnnQuery knnQuery = new KnnQuery("embedding", vector, 3, 1.0f, null);

        Query result = generator.visit(knnQuery);

        assertInstanceOf(KnnFloatVectorQuery.class, result);
    }

    @Test
    void knnQueryWithFilterConvertsFilterUsingVisitor() throws Exception {
        float[] vector = {1.0f, 0.0f};
        MatchQuery filterQuery = new MatchQuery("category", "fruit");
        KnnQuery knnQuery = new KnnQuery("embedding", vector, 4, filterQuery);

        Query result = generator.visit(knnQuery);

        Query expectedFilter = new TermQuery(new Term("category", "fruit"));
        assertEquals(new KnnFloatVectorQuery("embedding", vector, 4, expectedFilter), result);
    }

    @Test
    void knnQueryWithNullFilterPassesNullToLuceneQuery() throws Exception {
        float[] vector = {0.1f, 0.2f, 0.3f};
        KnnQuery knnQuery = new KnnQuery("embedding", vector, 7);

        Query result = generator.visit(knnQuery);

        assertEquals(new KnnFloatVectorQuery("embedding", vector, 7, null), result);
    }

    @Test
    void knnQueryPropagatesFieldAndKToLuceneQuery() throws Exception {
        float[] vector = {5.0f, 6.0f, 7.0f};
        KnnQuery knnQuery = new KnnQuery("otherEmbedding", vector, 42);

        Query result = generator.visit(knnQuery);

        assertEquals(new KnnFloatVectorQuery("otherEmbedding", vector, 42, null), result);
    }
}