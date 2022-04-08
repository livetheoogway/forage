package com.phonepe.platform.forage.search.engine.model.query.result;

import lombok.Value;

@Value
public class TotalResults {
    long total;
    Relation relation;
}
