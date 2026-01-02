/*
 * Copyright 2022. Live the Oogway, Tushar Naik
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
import com.livetheoogway.forage.models.query.search.score.ScoreFunction;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FunctionScoreQuery extends Query {
    Query baseQuery;
    ScoreFunction scoreFunction;
    Float boost;

    @JsonCreator
    public FunctionScoreQuery(@JsonProperty("baseQuery") final Query baseQuery,
                              @JsonProperty("scoreFunction") final ScoreFunction scoreFunction,
                              @JsonProperty("boost") final Float boost) {
        super(QueryType.FUNCTION_SCORE);
        this.baseQuery = baseQuery;
        this.scoreFunction = scoreFunction;
        this.boost = boost;
    }

    public FunctionScoreQuery(final Query baseQuery, final ScoreFunction scoreFunction) {
        this(baseQuery, scoreFunction, null);
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}