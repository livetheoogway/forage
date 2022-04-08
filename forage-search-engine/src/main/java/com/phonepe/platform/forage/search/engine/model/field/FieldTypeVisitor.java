package com.phonepe.platform.forage.search.engine.model.field;

public interface FieldTypeVisitor<T> {

    T text();

    T string();

    T lucene();


}
