package com.phonepe.platform.forage.search.engine.model.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("java:S112")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "FORAGE_SEARCH", value = ForageSearchQuery.class),
        @JsonSubTypes.Type(name = "PAGE", value = PageQuery.class)
})
@Getter
public abstract class ForageQuery {
    private ForageQueryType type;

    public abstract <T> T accept(ForageQueryVisitor<T> visitor) throws Exception;
}
