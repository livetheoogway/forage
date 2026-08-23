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

package com.livetheoogway.forage.models.result.field;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorFieldTest {

    @Test
    void testVectorFieldWithExplicitDimensionsAndSimilarity() {
        float[] vector = {1.0f, 2.0f, 3.0f};

        VectorField field = new VectorField("embedding", vector, 3, VectorSimilarity.EUCLIDEAN);

        assertEquals("embedding", field.getName());
        assertArrayEquals(vector, field.getVector());
        assertEquals(3, field.getDimensions());
        assertEquals(VectorSimilarity.EUCLIDEAN, field.getSimilarity());
        assertEquals(FieldType.VECTOR, field.getFieldType());
    }

    @Test
    void testVectorFieldInfersDimensionsAndDefaultsToCosine() {
        float[] vector = {0.1f, 0.2f, 0.3f, 0.4f};

        VectorField field = new VectorField("embedding", vector);

        assertEquals(4, field.getDimensions());
        assertEquals(VectorSimilarity.COSINE, field.getSimilarity());
        assertEquals(FieldType.VECTOR, field.getFieldType());
    }

    @Test
    void testVectorFieldInfersDimensionsWithExplicitSimilarity() {
        float[] vector = {0.5f, 0.5f};

        VectorField field = new VectorField("embedding", vector, VectorSimilarity.DOT_PRODUCT);

        assertEquals(2, field.getDimensions());
        assertEquals(VectorSimilarity.DOT_PRODUCT, field.getSimilarity());
    }

    @Test
    void testAcceptDelegatesToVisitor() {
        VectorField field = new VectorField("embedding", new float[]{1f, 2f});

        FieldVisitor<String> visitor = new FieldVisitor<>() {
            @Override
            public String visit(TextField textField) {
                return null;
            }

            @Override
            public String visit(StringField stringField) {
                return null;
            }

            @Override
            public String visit(FloatField floatField) {
                return null;
            }

            @Override
            public String visit(IntField intField) {
                return null;
            }

            @Override
            public String visit(VectorField vectorField) {
                return "visited-vector";
            }
        };

        assertEquals("visited-vector", field.accept(visitor));
    }

    @Test
    void testToStringContainsKeyDetails() {
        VectorField field = new VectorField("embedding", new float[]{1.0f, 2.0f}, VectorSimilarity.COSINE);

        String result = field.toString();

        assertTrue(result.startsWith("VECTOR["));
        assertTrue(result.contains("embedding"));
        assertTrue(result.contains("dim=2"));
        assertTrue(result.contains("sim=COSINE"));
        assertTrue(result.contains("1.0"));
        assertTrue(result.contains("2.0"));
    }

    @Test
    void testEqualsAndHashCodeConsiderAllFields() {
        VectorField field1 = new VectorField("embedding", new float[]{1f, 2f}, VectorSimilarity.COSINE);
        VectorField field2 = new VectorField("embedding", new float[]{1f, 2f}, VectorSimilarity.COSINE);
        VectorField differentVector = new VectorField("embedding", new float[]{9f, 9f}, VectorSimilarity.COSINE);
        VectorField differentSimilarity = new VectorField("embedding", new float[]{1f, 2f}, VectorSimilarity.EUCLIDEAN);

        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
        assertNotEquals(field1, differentVector);
        assertNotEquals(field1, differentSimilarity);
    }
}