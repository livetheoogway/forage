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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "FIELD_VALUE_FACTOR", value = FieldValueFactorFunction.class),
        @JsonSubTypes.Type(name = "CONSTANT_SCORE", value = ConstantScoreFunction.class),
        @JsonSubTypes.Type(name = "SCRIPT_SCORE", value = ScriptScoreFunction.class),
        @JsonSubTypes.Type(name = "RANDOM_SCORE", value = RandomScoreFunction.class),
        @JsonSubTypes.Type(name = "WEIGHTED_SCORE", value = WeightedScoreFunction.class),
        @JsonSubTypes.Type(name = "DECAY_FUNCTION", value = DecayFunction.class)
})
@Data
public abstract class ScoreFunction {
    private final ScoreFunctionType type;
}