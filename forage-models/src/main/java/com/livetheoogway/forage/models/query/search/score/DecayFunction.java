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
public class DecayFunction extends ScoreFunction {
    double origin;
    double scale;
    double offset;
    double decay;
    DecayType decayType;
    String field;

    @JsonCreator
    public DecayFunction(@JsonProperty("origin") final Double origin,
                         @JsonProperty("scale") final Double scale,
                         @JsonProperty("offset") final Double offset,
                         @JsonProperty("decay") final Double decay,
                         @JsonProperty("decayType") final DecayType decayType,
                         @JsonProperty("field") final String field) {
        super(ScoreFunctionType.DECAY_FUNCTION);
        this.origin = origin != null ? origin : 0.0;
        this.scale = scale != null ? scale : 1.0;
        this.offset = offset != null ? offset : 0.0;
        this.decay = decay != null ? decay : 0.5;
        this.decayType = decayType != null ? decayType : DecayType.EXP;
        this.field = field;
    }

    public DecayFunction(final String field, final double origin, final double scale) {
        this(origin, scale, 0.0, 0.5, DecayType.EXP, field);
    }
}