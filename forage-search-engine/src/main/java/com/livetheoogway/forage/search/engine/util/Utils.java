package com.livetheoogway.forage.search.engine.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

@UtilityClass
@Slf4j
public class Utils {

    public void closeSafe(Closeable closeable, String type) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            log.error("Error closing {}", type, e);
        }
    }
}
