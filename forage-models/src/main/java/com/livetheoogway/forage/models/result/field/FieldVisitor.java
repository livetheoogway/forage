package com.livetheoogway.forage.models.result.field;

public interface FieldVisitor<T> {
    T visit(TextField textField);

    T visit(StringField stringField);

    T visit(FloatField floatField);

    T visit(IntField intField);
}
