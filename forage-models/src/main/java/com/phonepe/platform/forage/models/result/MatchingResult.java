package com.phonepe.platform.forage.models.result;

import lombok.Value;

@Value
public class MatchingResult {
    String id;
    Object data;
    DocScore docScore;
}