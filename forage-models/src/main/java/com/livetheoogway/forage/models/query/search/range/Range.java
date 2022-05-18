package com.livetheoogway.forage.models.query.search.range;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "INT", value = IntRange.class),
        @JsonSubTypes.Type(name = "FLOAT", value = FloatRange.class)
})
@Data
public abstract class Range {
    private final RangeType rangeType;

    public abstract <T> T accept(RangeVisitor<T> rangeVisitor);
}
