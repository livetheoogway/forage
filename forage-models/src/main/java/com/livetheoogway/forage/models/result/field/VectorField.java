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

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Arrays;

/**
 * Represents a dense vector field for storing embeddings.
 * Used for vector similarity search (KNN) operations.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class VectorField extends Field {
    String name;
    float[] vector;
    int dimensions;
    VectorSimilarity similarity;

    /**
     * Creates a new VectorField with explicit dimensions and similarity function.
     *
     * @param name       the name of the field
     * @param vector     the dense vector (embedding)
     * @param dimensions the dimensionality of the vector
     * @param similarity the similarity function to use for search
     */
    public VectorField(final String name,
                       final float[] vector,
                       final int dimensions,
                       final VectorSimilarity similarity) {
        super(FieldType.VECTOR);
        this.name = name;
        this.vector = vector;
        this.dimensions = dimensions;
        this.similarity = similarity;
    }

    /**
     * Creates a new VectorField with dimensions inferred from the vector length.
     * Defaults to COSINE similarity.
     *
     * @param name   the name of the field
     * @param vector the dense vector (embedding)
     */
    public VectorField(final String name, final float[] vector) {
        this(name, vector, vector.length, VectorSimilarity.COSINE);
    }

    /**
     * Creates a new VectorField with dimensions inferred from the vector length.
     *
     * @param name       the name of the field
     * @param vector     the dense vector (embedding)
     * @param similarity the similarity function to use for search
     */
    public VectorField(final String name,
                       final float[] vector,
                       final VectorSimilarity similarity) {
        this(name, vector, vector.length, similarity);
    }

    @Override
    public <T> T accept(final FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "VECTOR[" +
                name +
                ":dim=" +
                dimensions +
                ",sim=" +
                similarity +
                ",values=" +
                Arrays.toString(vector) +
                "]";
    }
}
