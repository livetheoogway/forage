package com.phonepe.platform.forage.models.query;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.Max;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PageQuery extends ForageQuery {
    String page;

    @Max(1024)
    int size;

    public PageQuery(final String page, final int size) {
        super(ForageQueryType.PAGE);
        this.page = page;
        this.size = size;
    }

    @Override
    public <T> T accept(final ForageQueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
