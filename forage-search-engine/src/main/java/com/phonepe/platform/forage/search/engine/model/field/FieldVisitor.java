package com.phonepe.platform.forage.search.engine.model.field;

public interface FieldVisitor<T> {
    T visit(TextField textField);

    T visit(StringField stringField);

    T visit(LuceneField luceneField);

    T visit(FloatField floatField);

    T visit(IntField intField);
}
