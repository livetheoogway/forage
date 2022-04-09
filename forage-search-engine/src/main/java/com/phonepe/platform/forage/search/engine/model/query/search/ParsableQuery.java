package com.phonepe.platform.forage.search.engine.model.query.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
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
    public <T> T accept(final QueryVisitor<T> visitor) throws ForageSearchError {
        return visitor.visit(this);
    }
}
