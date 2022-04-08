package com.phonepe.platform.forage.search.engine.model.query.result;

import lombok.Value;

@Value
public class DocScore {
    float score;
    int doc;
    int shardIndex;
}
