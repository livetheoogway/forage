package com.phonepe.platform.forage.search.engine.model.query.search.range;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FloatRange extends Range {
    /* inclusive */
    float low;

    /* inclusive */
    float high;

    public FloatRange(final float low, final float high) {
        super(RangeType.FLOAT);
        this.low = low;
        this.high = high;
    }

    @Override
    public <T> T accept(final RangeVisitor<T> rangeVisitor) {
        return rangeVisitor.visit(this);
    }
}
