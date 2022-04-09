package com.phonepe.platform.forage.search.engine.model.field;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "fieldType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "TEXT", value = TextField.class),
        @JsonSubTypes.Type(name = "STRING", value = StringField.class),
        @JsonSubTypes.Type(name = "INT", value = IntField.class),
        @JsonSubTypes.Type(name = "FLOAT", value = FloatField.class)
})
@Getter
public abstract class Field {
    private final FieldType fieldType;

    public abstract <T> T accept(FieldVisitor<T> fieldVisitor);
}
