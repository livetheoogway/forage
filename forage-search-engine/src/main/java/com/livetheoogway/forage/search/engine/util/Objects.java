package com.livetheoogway.forage.search.engine.util;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

@UtilityClass
public class Objects {

    public static <T> void whenTrue(final T value, final Predicate<T> condition, final Consumer<T> consumer) {
        if (condition.test(value)) {
            consumer.accept(value);
        }
    }

    public static <T> void whenNotNull(@Nullable final T value, final Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
