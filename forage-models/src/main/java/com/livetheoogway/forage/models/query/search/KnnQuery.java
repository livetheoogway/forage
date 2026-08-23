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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Arrays;

/**
 * K-Nearest Neighbors query for vector similarity search.
 * Finds the k most similar documents based on vector distance/similarity.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KnnQuery extends Query {
    /**
     * The name of the vector field to search.
     */
    String field;

    /**
     * The query vector to find nearest neighbors for.
     */
    float[] queryVector;

    /**
     * The number of nearest neighbors to return.
     */
    int k;

    /**
     * Optional boost factor to apply to the scores.
     */
    Float boost;

    /**
     * Optional filter query to pre-filter documents before vector search.
     * Enables hybrid search (combining keyword filters with vector similarity).
     */
    Query filter;

    @JsonCreator
    public KnnQuery(@JsonProperty("field") final String field,
                    @JsonProperty("queryVector") final float[] queryVector,
                    @JsonProperty("k") final int k,
                    @JsonProperty("boost") final Float boost,
                    @JsonProperty("filter") final Query filter) {
        super(QueryType.KNN);
        this.field = field;
        this.queryVector = queryVector;
        this.k = k;
        this.boost = boost;
        this.filter = filter;
    }

    /**
     * Creates a KNN query without boost or filter.
     */
    public KnnQuery(final String field, final float[] queryVector, final int k) {
        this(field, queryVector, k, null, null);
    }

    /**
     * Creates a KNN query with a filter for hybrid search.
     */
    public KnnQuery(final String field, final float[] queryVector, final int k, final Query filter) {
        this(field, queryVector, k, null, filter);
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("KnnQuery[field=")
                .append(field)
                .append(", k=")
                .append(k)
                .append(", vectorDim=")
                .append(queryVector != null ? queryVector.length : 0)
                .append(", boost=")
                .append(boost)
                .append(", hasFilter=")
                .append(filter != null)
                .append("]")
                .toString();
    }
}
