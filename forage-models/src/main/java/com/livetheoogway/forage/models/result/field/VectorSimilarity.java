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

/**
 * Defines the similarity function used for vector similarity search.
 * This determines how the distance/similarity between vectors is computed.
 */
public enum VectorSimilarity {
    /**
     * Cosine similarity - measures the cosine of the angle between two vectors.
     * Values range from -1 (opposite) to 1 (identical direction).
     * Good for text embeddings where magnitude doesn't matter.
     */
    COSINE,

    /**
     * Dot product similarity - the sum of element-wise products.
     * Faster than cosine as it doesn't normalize vectors.
     * Requires vectors to be normalized for comparable results.
     */
    DOT_PRODUCT,

    /**
     * Euclidean distance - the straight-line distance between two points.
     * Lower values indicate more similar vectors.
     */
    EUCLIDEAN
}
