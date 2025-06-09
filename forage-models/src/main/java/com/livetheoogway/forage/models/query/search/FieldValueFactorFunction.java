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
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FieldValueFactorFunction extends ScoreFunction {
    String field;
    Float factor;
    Float missing;

    @JsonCreator
    public FieldValueFactorFunction(@JsonProperty("field") final String field,
                                    @JsonProperty("factor") final Float factor,
                                    @JsonProperty("missing") final Float missing) {
        super(ScoreFunctionType.FIELD_VALUE_FACTOR);
        this.field = field;
        this.factor = factor != null ? factor : 1.0f;
        this.missing = missing != null ? missing : 1.0f;
    }

    public FieldValueFactorFunction(final String field) {
        this(field, 1.0f, 1.0f);
    }

    public FieldValueFactorFunction(final String field, final Float factor) {
        this(field, factor, 1.0f);
    }
}