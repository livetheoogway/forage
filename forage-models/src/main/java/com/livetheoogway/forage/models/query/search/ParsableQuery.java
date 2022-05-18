package com.livetheoogway.forage.models.query.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ParsableQuery extends Query {
    String field;
    String queryString;

    @JsonCreator
    public ParsableQuery(@JsonProperty("field") final String field,
                         @JsonProperty("queryString") final String queryString) {
        super(QueryType.PARSABLE_QUERY);
        this.field = field;
        this.queryString = queryString;
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
