package com.phonepe.platform.forage.models.result.field;

public interface FieldTypeVisitor<T> {

    T text();

    T string();

    T floatPoint();

    T intPoint();

}
