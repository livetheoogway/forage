package com.phonepe.platform.forage.models.query;

import com.phonepe.platform.forage.models.query.search.Query;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.Max;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ForageSearchQuery extends ForageQuery {
    Query query;

    @Max(1024)
    int size;

    public ForageSearchQuery(final Query query, final int size) {
        super(ForageQueryType.FORAGE_SEARCH);
        this.query = query;
        this.size = size;
    }

    @Override
    public <T> T accept(final ForageQueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
