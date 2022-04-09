package com.phonepe.platform.forage.search.engine.model.query.search.range;

public interface RangeVisitor<T> {
    T visit(IntRange intRange);

    T visit(FloatRange floatRange);
}
