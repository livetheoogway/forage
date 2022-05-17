package com.phonepe.platform.forage.models.query.search;

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
        @JsonSubTypes.Type(name = "PARSABLE_QUERY", value = ParsableQuery.class),
        @JsonSubTypes.Type(name = "RANGE", value = RangeQuery.class),
        @JsonSubTypes.Type(name = "BOOLEAN", value = BooleanQuery.class)
})
@Data
public abstract class Query {
    private final QueryType type;
    public abstract <T> T accept(QueryVisitor<T> visitor) throws Exception;
}
