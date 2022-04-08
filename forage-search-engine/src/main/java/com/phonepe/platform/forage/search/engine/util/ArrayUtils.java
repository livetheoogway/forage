package com.phonepe.platform.forage.search.engine.util;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class ArrayUtils {
    public <T> Optional<T> last(T[] array) {
        if (array == null || array.length <= 0) {
            return Optional.empty();
        }
        return Optional.of(array[array.length - 1]);
    }
}
