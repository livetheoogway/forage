package com.livetheoogway.forage.models.query.search.range;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IntRange extends Range {
    /* inclusive */
    int low;
    /* inclusive */
    int high;

    public IntRange(final int low, final int high) {
        super(RangeType.INT);
        this.low = low;
        this.high = high;
    }

    @Override
    public <T> T accept(final RangeVisitor<T> rangeVisitor) {
        return rangeVisitor.visit(this);
    }
}
