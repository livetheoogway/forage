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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings("java:S112")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "MATCH", value = MatchQuery.class),
        @JsonSubTypes.Type(name = "FUZZY_MATCH", value = FuzzyMatchQuery.class),
        @JsonSubTypes.Type(name = "MATCH_ALL", value = MatchAllQuery.class),
        @JsonSubTypes.Type(name = "PARSABLE_QUERY", value = ParsableQuery.class),
        @JsonSubTypes.Type(name = "RANGE", value = RangeQuery.class),
        @JsonSubTypes.Type(name = "PHRASE", value = PhraseMatchQuery.class),
        @JsonSubTypes.Type(name = "PREFIX", value = PrefixMatchQuery.class),
        @JsonSubTypes.Type(name = "BOOLEAN", value = BooleanQuery.class)
})
@Data
public abstract class Query {
    private final QueryType type;
    public abstract <T> T accept(QueryVisitor<T> visitor) throws Exception;
}
