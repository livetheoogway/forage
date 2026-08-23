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

package com.livetheoogway.forage.search.engine.lucene.field;

import com.livetheoogway.forage.models.result.field.VectorField;
import com.livetheoogway.forage.models.result.field.VectorSimilarity;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests for the VectorField handling added to {@link LuceneFieldHandler}.
 */
class LuceneFieldHandlerTest {

    private final LuceneFieldHandler handler = new LuceneFieldHandler();

    @Test
    void testVisitVectorFieldProducesSingleKnnFloatVectorField() {
        float[] vector = {1.0f, 2.0f, 3.0f};
        VectorField vectorField = new VectorField("embedding", vector, VectorSimilarity.COSINE);

        List<IndexableField> fields = handler.visit(vectorField);

        assertEquals(1, fields.size());
        assertInstanceOf(KnnFloatVectorField.class, fields.get(0));
        assertEquals("embedding", fields.get(0).name());
    }

    @Test
    void testCosineSimilarityIsMappedToLuceneCosine() {
        VectorField vectorField = new VectorField("embedding", new float[]{1.0f, 0.0f}, VectorSimilarity.COSINE);

        List<IndexableField> fields = handler.visit(vectorField);

        assertEquals(VectorSimilarityFunction.COSINE, fields.get(0).fieldType().vectorSimilarityFunction());
    }

    @Test
    void testDotProductSimilarityIsMappedToLuceneDotProduct() {
        VectorField vectorField = new VectorField("embedding", new float[]{1.0f, 0.0f}, VectorSimilarity.DOT_PRODUCT);

        List<IndexableField> fields = handler.visit(vectorField);

        assertEquals(VectorSimilarityFunction.DOT_PRODUCT, fields.get(0).fieldType().vectorSimilarityFunction());
    }

    @Test
    void testEuclideanSimilarityIsMappedToLuceneEuclidean() {
        VectorField vectorField = new VectorField("embedding", new float[]{1.0f, 0.0f}, VectorSimilarity.EUCLIDEAN);

        List<IndexableField> fields = handler.visit(vectorField);

        assertEquals(VectorSimilarityFunction.EUCLIDEAN, fields.get(0).fieldType().vectorSimilarityFunction());
    }
}