package com.phonepe.platform.forage.search.engine.model.query.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "IS", value = IsQuery.class),
        @JsonSubTypes.Type(name = "PARSABLE_QUERY", value = ParsableQuery.class),
        @JsonSubTypes.Type(name = "BOOLEAN", value = BooleanQuery.class)
})
@Getter
public abstract class Query {
    private QueryType type;

    public abstract <T> T accept(QueryVisitor<T> visitor) throws ForageSearchError;
}
