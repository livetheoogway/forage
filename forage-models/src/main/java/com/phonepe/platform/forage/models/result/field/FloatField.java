package com.phonepe.platform.forage.models.result.field;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FloatField extends Field {
    String name;
    float[] points;

    public FloatField(final String name, final float[] points) {
        super(FieldType.FLOAT);
        this.name = name;
        this.points = points;
    }

    @Override
    public <T> T accept(final FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }
}
