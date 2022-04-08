package com.phonepe.platform.forage.search.engine.model.query.search;

import com.phonepe.platform.forage.search.engine.model.query.result.DocScore;
import lombok.Value;

@Value
public class MatchingResult {
    Object data;
    DocScore docScore;
}



