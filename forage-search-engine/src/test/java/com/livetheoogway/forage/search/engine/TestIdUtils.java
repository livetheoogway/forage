package com.livetheoogway.forage.search.engine;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestIdUtils {
    public String generateBookId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
