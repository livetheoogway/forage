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

import com.google.common.collect.ImmutableList;
import com.livetheoogway.forage.models.DataId;
import com.livetheoogway.forage.models.query.ForageQuery;
import com.livetheoogway.forage.models.query.util.QueryBuilder;
import com.livetheoogway.forage.models.result.ForageQueryResult;
import com.livetheoogway.forage.models.result.MatchingResult;
import com.livetheoogway.forage.models.result.field.Field;
import com.livetheoogway.forage.models.result.field.TextField;
import com.livetheoogway.forage.models.result.field.VectorField;
import com.livetheoogway.forage.models.result.field.VectorSimilarity;
import com.livetheoogway.forage.search.engine.TestUtils;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.IndexableDocument;
import lombok.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comprehensive tests for vector search (KNN) functionality.
 */
class VectorSearchTest {

    private static final int VECTOR_DIMENSIONS = 4;

    // ==================== BASIC VECTOR SEARCH TESTS ====================

    @Test
    void testBasicVectorSearch() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("doc-1", "Apple", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("doc-2", "Banana", new float[]{0.9f, 0.1f, 0.0f, 0.0f}),
                vectorDoc("doc-3", "Orange", new float[]{0.0f, 1.0f, 0.0f, 0.0f}))) {

            // Query vector similar to doc-1 and doc-2
            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 2)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            Assertions.assertEquals(2, result.getMatchingResults().size());
            // doc-1 should be most similar (exact match)
            Assertions.assertEquals("doc-1", result.getMatchingResults().get(0).getId());
        }
    }

    @Test
    void testVectorSearchReturnsMostSimilarDocuments() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("close", "Close", new float[]{0.9f, 0.1f, 0.0f, 0.0f}),
                vectorDoc("exact", "Exact", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("far", "Far", new float[]{0.0f, 0.0f, 1.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 3)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            // Most similar should be first
            List<String> orderedIds = result.getMatchingResults().stream()
                    .map(MatchingResult::getId)
                    .toList();

            Assertions.assertEquals("exact", orderedIds.get(0), "Exact match should be first");
            Assertions.assertEquals("close", orderedIds.get(1), "Close vector should be second");
            Assertions.assertEquals("far", orderedIds.get(2), "Far vector should be last");
        }
    }

    @Test
    void testVectorSearchWithKLimitingResults() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("doc-1", "Doc1", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("doc-2", "Doc2", new float[]{0.9f, 0.1f, 0.0f, 0.0f}),
                vectorDoc("doc-3", "Doc3", new float[]{0.8f, 0.2f, 0.0f, 0.0f}),
                vectorDoc("doc-4", "Doc4", new float[]{0.7f, 0.3f, 0.0f, 0.0f}),
                vectorDoc("doc-5", "Doc5", new float[]{0.6f, 0.4f, 0.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            // Request only 2 results
            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 2)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            Assertions.assertEquals(2, result.getMatchingResults().size());
            Assertions.assertEquals("doc-1", result.getMatchingResults().get(0).getId());
            Assertions.assertEquals("doc-2", result.getMatchingResults().get(1).getId());
        }
    }

    // ==================== SIMILARITY FUNCTION TESTS ====================

    @Test
    void testCosineSimiliarity() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDocWithSimilarity("same-direction", "Same", 
                        new float[]{2.0f, 0.0f, 0.0f, 0.0f}, VectorSimilarity.COSINE),
                vectorDocWithSimilarity("orthogonal", "Orthogonal", 
                        new float[]{0.0f, 1.0f, 0.0f, 0.0f}, VectorSimilarity.COSINE))) {

            // Cosine similarity: direction matters, not magnitude
            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 2)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            // Same direction (even with different magnitude) should be most similar
            Assertions.assertEquals("same-direction", result.getMatchingResults().get(0).getId());
        }
    }

    @Test
    void testDotProductSimilarity() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDocWithSimilarity("large-magnitude", "Large", 
                        new float[]{2.0f, 0.0f, 0.0f, 0.0f}, VectorSimilarity.DOT_PRODUCT),
                vectorDocWithSimilarity("small-magnitude", "Small", 
                        new float[]{0.5f, 0.0f, 0.0f, 0.0f}, VectorSimilarity.DOT_PRODUCT))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 2)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            // Dot product: magnitude matters, larger vector should score higher
            Assertions.assertEquals("large-magnitude", result.getMatchingResults().get(0).getId());
        }
    }

    @Test
    void testEuclideanSimilarity() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDocWithSimilarity("near", "Near", 
                        new float[]{0.9f, 0.1f, 0.0f, 0.0f}, VectorSimilarity.EUCLIDEAN),
                vectorDocWithSimilarity("far", "Far", 
                        new float[]{0.0f, 1.0f, 0.5f, 0.5f}, VectorSimilarity.EUCLIDEAN))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 2)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            // Euclidean: smaller distance = more similar
            Assertions.assertEquals("near", result.getMatchingResults().get(0).getId());
        }
    }

    // ==================== HYBRID SEARCH TESTS ====================

    @Test
    void testHybridSearchWithTextFilter() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("apple-doc", "apple", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("banana-doc", "banana", new float[]{0.99f, 0.01f, 0.0f, 0.0f}),
                vectorDoc("apple-doc-2", "apple", new float[]{0.8f, 0.2f, 0.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            // Without filter - banana-doc would be second due to similarity
            ForageQuery unfiltered = QueryBuilder.knnQuery("embedding", queryVector, 3)
                    .buildForageQuery();
            ForageQueryResult<VectorDocument> unfilteredResult = engine.search(unfiltered);
            Assertions.assertEquals(3, unfilteredResult.getMatchingResults().size());

            // With filter - only "apple" content documents
            ForageQuery filtered = QueryBuilder.knnQuery("embedding", queryVector, 3)
                    .filter(QueryBuilder.matchQuery("content", "apple").build())
                    .buildForageQuery();
            ForageQueryResult<VectorDocument> filteredResult = engine.search(filtered);

            Assertions.assertEquals(2, filteredResult.getMatchingResults().size());
            // All results should be apple documents
            for (MatchingResult<VectorDocument> match : filteredResult.getMatchingResults()) {
                Assertions.assertTrue(match.getData().getContent().contains("apple"));
            }
        }
    }

    @Test
    void testHybridSearchWithBooleanFilter() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("fruit-apple", "apple fruit", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("fruit-banana", "banana fruit", new float[]{0.95f, 0.05f, 0.0f, 0.0f}),
                vectorDoc("veggie-carrot", "carrot vegetable", new float[]{0.9f, 0.1f, 0.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            // Filter for "fruit" content only
            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 3)
                    .filter(QueryBuilder.matchQuery("content", "fruit").build())
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            Assertions.assertEquals(2, result.getMatchingResults().size());
            // Verify all results contain "fruit"
            for (MatchingResult<VectorDocument> match : result.getMatchingResults()) {
                Assertions.assertTrue(match.getData().getContent().contains("fruit"));
            }
        }
    }

    // ==================== BOOST TESTS ====================

    @Test
    void testKnnQueryWithBoost() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("doc-1", "Doc1", new float[]{1.0f, 0.0f, 0.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            // Without boost
            ForageQuery queryNoBoost = QueryBuilder.knnQuery("embedding", queryVector, 1)
                    .buildForageQuery();
            ForageQueryResult<VectorDocument> resultNoBoost = engine.search(queryNoBoost);
            float scoreNoBoost = resultNoBoost.getMatchingResults().get(0).getDocScore().getScore();

            // With boost of 2.0
            ForageQuery queryWithBoost = QueryBuilder.knnQuery("embedding", queryVector, 1)
                    .boost(2.0f)
                    .buildForageQuery();
            ForageQueryResult<VectorDocument> resultWithBoost = engine.search(queryWithBoost);
            float scoreWithBoost = resultWithBoost.getMatchingResults().get(0).getDocScore().getScore();

            Assertions.assertEquals(scoreNoBoost * 2.0f, scoreWithBoost, 0.001f,
                    "Boost should multiply the score");
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    void testVectorSearchOnEmptyIndex() throws Exception {
        InMemoryHashStore<VectorDocument> dataStore = new InMemoryHashStore<>();
        try (ForageLuceneSearchEngine<VectorDocument> engine = ForageSearchEngineBuilder.<VectorDocument>builder()
                .withObjectMapper(TestUtils.mapper())
                .withDataStore(dataStore).build()) {
            engine.flush();

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};
            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 10)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            Assertions.assertEquals(0, result.getMatchingResults().size());
            Assertions.assertEquals(0, result.getTotal().getTotal());
        }
    }

    @Test
    void testVectorSearchWithSingleDocument() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("only-doc", "Only", new float[]{0.5f, 0.5f, 0.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 10)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            Assertions.assertEquals(1, result.getMatchingResults().size());
            Assertions.assertEquals("only-doc", result.getMatchingResults().get(0).getId());
        }
    }

    @Test
    void testVectorSearchWithIdenticalVectors() throws Exception {
        try (ForageLuceneSearchEngine<VectorDocument> engine = buildVectorEngine(
                vectorDoc("doc-1", "First", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("doc-2", "Second", new float[]{1.0f, 0.0f, 0.0f, 0.0f}),
                vectorDoc("doc-3", "Third", new float[]{1.0f, 0.0f, 0.0f, 0.0f}))) {

            float[] queryVector = new float[]{1.0f, 0.0f, 0.0f, 0.0f};

            ForageQuery query = QueryBuilder.knnQuery("embedding", queryVector, 3)
                    .buildForageQuery();

            ForageQueryResult<VectorDocument> result = engine.search(query);

            Assertions.assertEquals(3, result.getMatchingResults().size());
            // All documents should have equal similarity scores
            float firstScore = result.getMatchingResults().get(0).getDocScore().getScore();
            for (MatchingResult<VectorDocument> match : result.getMatchingResults()) {
                Assertions.assertEquals(firstScore, match.getDocScore().getScore(), 0.001f,
                        "Identical vectors should have equal scores");
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private ForageLuceneSearchEngine<VectorDocument> buildVectorEngine(VectorDocument... docs) throws ForageSearchError {
        InMemoryHashStore<VectorDocument> store = new InMemoryHashStore<>();
        ForageLuceneSearchEngine<VectorDocument> engine = ForageSearchEngineBuilder.<VectorDocument>builder()
                .withObjectMapper(TestUtils.mapper())
                .withDataStore(store)
                .build();

        List<VectorDocument> docList = Arrays.asList(docs);
        List<IndexableDocument> indexableDocs = docList.stream()
                .map(doc -> ForageDocument.builder()
                        .id(doc.id())
                        .fields(doc.fields())
                        .build())
                .collect(Collectors.toList());

        store.store(docList);
        engine.index(indexableDocs);
        engine.flush();
        return engine;
    }

    private VectorDocument vectorDoc(String id, String content, float[] vector) {
        return new VectorDocument(id, content, vector, VectorSimilarity.COSINE);
    }

    private VectorDocument vectorDocWithSimilarity(String id, String content, float[] vector, VectorSimilarity similarity) {
        return new VectorDocument(id, content, vector, similarity);
    }

    @Value
    static class VectorDocument implements DataId {
        String id;
        String content;
        float[] vector;
        VectorSimilarity similarity;

        public List<Field> fields() {
            return ImmutableList.of(
                    new TextField("content", content),
                    new VectorField("embedding", vector, similarity)
            );
        }

        @Override
        public String id() {
            return id;
        }
    }
}
