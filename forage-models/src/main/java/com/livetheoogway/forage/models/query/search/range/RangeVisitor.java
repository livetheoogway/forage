package com.livetheoogway.forage.models.query.search.range;

public interface RangeVisitor<T> {
    T visit(IntRange intRange);

    T visit(FloatRange floatRange);
}
