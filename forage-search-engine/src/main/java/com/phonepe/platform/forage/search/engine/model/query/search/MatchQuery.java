package com.phonepe.platform.forage.search.engine.model.query.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MatchQuery extends Query {
    String field;
    String value;

    @JsonCreator
    public MatchQuery(@JsonProperty("field") final String field, @JsonProperty("value") final String value) {
        super(QueryType.MATCH);
        this.field = field;
        this.value = value;
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
