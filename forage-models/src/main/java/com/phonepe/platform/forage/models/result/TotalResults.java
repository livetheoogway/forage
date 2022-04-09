package com.phonepe.platform.forage.models.result;

import lombok.Value;

@Value
public class TotalResults {
    long total;
    Relation relation;
}
