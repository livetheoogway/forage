package com.phonepe.platform.forage.search.engine.model.query.search;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BooleanQuery extends Query {
    List<Query> queries;
    ClauseType clauseType;

    public BooleanQuery(final List<Query> queries, final ClauseType clauseType) {
        super(QueryType.BOOLEAN);
        this.queries = queries;
        this.clauseType = clauseType;
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws ForageSearchError {
        return visitor.visit(this);
    }
}
