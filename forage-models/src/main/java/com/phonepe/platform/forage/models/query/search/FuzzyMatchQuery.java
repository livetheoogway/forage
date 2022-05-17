package com.phonepe.platform.forage.models.query.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FuzzyMatchQuery extends Query {
    String field;
    String value;

    @JsonCreator
    public FuzzyMatchQuery(@JsonProperty("field") final String field, @JsonProperty("value") final String value) {
        super(QueryType.FUZZY_MATCH);
        this.field = field;
        this.value = value;
    }

    @Override
    public <T> T accept(final QueryVisitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
