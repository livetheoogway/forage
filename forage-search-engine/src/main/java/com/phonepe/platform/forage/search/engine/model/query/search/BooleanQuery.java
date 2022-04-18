package com.phonepe.platform.forage.search.engine.model.query.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@SuppressWarnings("java:S112")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BooleanQuery extends Query {
    List<Query> queries;
    ClauseType clauseType;

    @JsonCreator
    public BooleanQuery(@JsonProperty("queries") final List<Query> queries,
                        @JsonProperty("clauseType") final ClauseType clauseType) {
        super(QueryType.BOOLEAN);
        this.queries = queries;
        this.clauseType = clauseType;
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
