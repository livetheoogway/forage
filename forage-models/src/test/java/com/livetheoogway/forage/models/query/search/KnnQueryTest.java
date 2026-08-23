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

class KnnQueryTest {

    @Test
    void testKnnQueryWithAllFields() {
        float[] vector = {0.1f, 0.2f, 0.3f};
        MatchQuery filter = new MatchQuery("category", "fruit");

        KnnQuery query = new KnnQuery("embedding", vector, 5, 2.0f, filter);

        assertEquals("embedding", query.getField());
        assertArrayEquals(vector, query.getQueryVector());
        assertEquals(5, query.getK());
        assertEquals(2.0f, query.getBoost());
        assertEquals(filter, query.getFilter());
        assertEquals(QueryType.KNN, query.getType());
    }

    @Test
    void testKnnQueryWithoutBoostOrFilter() {
        float[] vector = {1.0f, 0.0f};
        KnnQuery query = new KnnQuery("embedding", vector, 3);

        assertEquals("embedding", query.getField());
        assertArrayEquals(vector, query.getQueryVector());
        assertEquals(3, query.getK());
        assertNull(query.getBoost());
        assertNull(query.getFilter());
        assertEquals(QueryType.KNN, query.getType());
    }

    @Test
    void testKnnQueryWithFilterButNoBoost() {
        float[] vector = {0.5f, 0.5f};
        MatchQuery filter = new MatchQuery("category", "vegetable");

        KnnQuery query = new KnnQuery("embedding", vector, 10, filter);

        assertEquals("embedding", query.getField());
        assertEquals(10, query.getK());
        assertNull(query.getBoost());
        assertEquals(filter, query.getFilter());
        assertEquals(QueryType.KNN, query.getType());
    }

    @Test
    void testAcceptDelegatesToVisitor() throws Exception {
        KnnQuery query = new KnnQuery("embedding", new float[]{1f, 2f}, 5);

        QueryVisitor<String> visitor = new QueryVisitor<>() {
            @Override
            public String visit(BooleanQuery booleanQuery) {
                return null;
            }

            @Override
            public String visit(MatchQuery matchQuery) {
                return null;
            }

            @Override
            public String visit(ParsableQuery parsableQuery) {
                return null;
            }

            @Override
            public String visit(RangeQuery rangeQuery) {
                return null;
            }

            @Override
            public String visit(FuzzyMatchQuery fuzzyMatchQuery) {
                return null;
            }

            @Override
            public String visit(PhraseMatchQuery phraseMatchQuery) {
                return null;
            }

            @Override
            public String visit(MatchAllQuery matchAllQuery) {
                return null;
            }

            @Override
            public String visit(PrefixMatchQuery prefixMatchQuery) {
                return null;
            }

            @Override
            public String visit(FunctionScoreQuery functionScoreQuery) {
                return null;
            }

            @Override
            public String visit(KnnQuery knnQuery) {
                return "visited-knn";
            }
        };

        assertEquals("visited-knn", query.accept(visitor));
    }

    @Test
    void testToStringContainsKeyDetails() {
        float[] vector = {1f, 2f, 3f, 4f};
        KnnQuery query = new KnnQuery("embedding", vector, 5, 1.5f, null);

        String result = query.toString();

        assertTrue(result.contains("field=embedding"));
        assertTrue(result.contains("k=5"));
        assertTrue(result.contains("vectorDim=4"));
        assertTrue(result.contains("boost=1.5"));
        assertTrue(result.contains("hasFilter=false"));
    }

    @Test
    void testToStringReflectsPresenceOfFilter() {
        float[] vector = {1f, 2f};
        KnnQuery query = new KnnQuery("embedding", vector, 5, new MatchQuery("a", "b"));

        String result = query.toString();

        assertTrue(result.contains("hasFilter=true"));
    }

    @Test
    void testToStringHandlesNullVector() {
        KnnQuery query = new KnnQuery("embedding", null, 5, null, null);

        String result = query.toString();

        assertTrue(result.contains("vectorDim=0"));
    }

    @Test
    void testEqualsAndHashCodeConsiderAllFields() {
        MatchQuery filter = new MatchQuery("a", "b");

        KnnQuery query1 = new KnnQuery("field", new float[]{1f, 2f}, 5, 1.0f, filter);
        KnnQuery query2 = new KnnQuery("field", new float[]{1f, 2f}, 5, 1.0f, filter);
        KnnQuery differentK = new KnnQuery("field", new float[]{1f, 2f}, 6, 1.0f, filter);
        KnnQuery differentVector = new KnnQuery("field", new float[]{9f, 9f}, 5, 1.0f, filter);

        assertEquals(query1, query2);
        assertEquals(query1.hashCode(), query2.hashCode());
        assertNotEquals(query1, differentK);
        assertNotEquals(query1, differentVector);
    }
}