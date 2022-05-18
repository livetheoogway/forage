package com.livetheoogway.forage.models.result.field;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Use this for indexing fields for full text search
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TextField extends Field {
    String name;
    String value;

    public TextField(String name, String value) {
        super(FieldType.TEXT);
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T accept(FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }
}
