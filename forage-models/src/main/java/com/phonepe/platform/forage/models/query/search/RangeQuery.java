package com.phonepe.platform.forage.models.query.search;

import com.phonepe.platform.forage.models.query.search.range.Range;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RangeQuery extends Query {
    String field;
    Range range;

    public RangeQuery(final String field, final Range range) {
        super(QueryType.RANGE);
        this.field = field;
        this.range = range;
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
