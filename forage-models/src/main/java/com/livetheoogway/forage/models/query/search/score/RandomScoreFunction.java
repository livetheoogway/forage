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

package com.livetheoogway.forage.models.query.search.score;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RandomScoreFunction extends ScoreFunction {
    Long seed;
    String field;

    @JsonCreator
    public RandomScoreFunction(@JsonProperty("seed") final Long seed,
                               @JsonProperty("field") final String field) {
        super(ScoreFunctionType.RANDOM_SCORE);
        this.seed = seed != null ? seed : 1L;
        this.field = field;
    }

    public RandomScoreFunction(final String field) {
        this(1L, field);
    }
}