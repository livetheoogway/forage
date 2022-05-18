package com.livetheoogway.forage.models.result.field;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IntField extends Field{
    String name;
    int[] points;

    public IntField(final String name, final int[] points) {
        super(FieldType.INT);
        this.name = name;
        this.points = points;
    }

    @Override
    public <T> T accept(final FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }
}
