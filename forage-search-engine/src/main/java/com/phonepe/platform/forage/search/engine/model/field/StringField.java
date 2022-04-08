package com.phonepe.platform.forage.search.engine.model.field;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Use this field to store fields verbatim (without analysis)
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StringField extends Field {
    String name;
    String value;

    public StringField(final String name, final String value) {
        super(FieldType.STRING);
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T accept(final FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }
}
