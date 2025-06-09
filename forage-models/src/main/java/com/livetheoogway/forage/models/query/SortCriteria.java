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

package com.livetheoogway.forage.models.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class SortCriteria {
    String field;
    SortOrder order;
    SortType type;

    @JsonCreator
    public SortCriteria(@JsonProperty("field") final String field,
                        @JsonProperty("order") final SortOrder order,
                        @JsonProperty("type") final SortType type) {
        this.field = field;
        this.order = order != null ? order : SortOrder.DESC;
        this.type = type != null ? type : SortType.SCORE;
    }

    public SortCriteria(final String field, final SortOrder order) {
        this(field, order, SortType.FIELD);
    }

    public SortCriteria(final String field) {
        this(field, SortOrder.DESC, SortType.FIELD);
    }

    public static SortCriteria byScore() {
        return new SortCriteria(null, SortOrder.DESC, SortType.SCORE);
    }

    public static SortCriteria byScore(final SortOrder order) {
        return new SortCriteria(null, order, SortType.SCORE);
    }
}