package com.livetheoogway.forage.models.result;

import lombok.Value;

@Value
public class MatchingResult<D> {
    String id;
    D data;
    DocScore docScore;
}