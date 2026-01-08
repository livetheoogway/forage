/*
 * Copyright 2026. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.search.engine.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectsTest {

    @Test
    void testWhenTrue_conditionIsTrue_consumerInvoked() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);
        AtomicReference<String> capturedValue = new AtomicReference<>();

        Objects.whenTrue("test-value", value -> true, value -> {
            consumerCalled.set(true);
            capturedValue.set(value);
        });

        assertTrue(consumerCalled.get());
        assertEquals("test-value", capturedValue.get());
    }

    @Test
    void testWhenTrue_conditionIsFalse_consumerNotInvoked() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);

        Objects.whenTrue("test-value", value -> false, value -> consumerCalled.set(true));

        assertFalse(consumerCalled.get());
    }

    @Test
    void testWhenTrue_withComplexCondition() {
        AtomicReference<Integer> result = new AtomicReference<>(0);

        Objects.whenTrue(10, value -> value > 5, value -> result.set(value * 2));

        assertEquals(20, result.get());
    }

    @Test
    void testWhenTrue_conditionChecksBoundary() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);

        Objects.whenTrue(5, value -> value >= 5, value -> consumerCalled.set(true));

        assertTrue(consumerCalled.get());
    }

    @Test
    void testWhenNotNull_valueIsNotNull_consumerInvoked() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);
        AtomicReference<String> capturedValue = new AtomicReference<>();

        Objects.whenNotNull("non-null-value", value -> {
            consumerCalled.set(true);
            capturedValue.set(value);
        });

        assertTrue(consumerCalled.get());
        assertEquals("non-null-value", capturedValue.get());
    }

    @Test
    void testWhenNotNull_valueIsNull_consumerNotInvoked() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);

        Objects.whenNotNull(null, value -> consumerCalled.set(true));

        assertFalse(consumerCalled.get());
    }

    @Test
    void testWhenNotNull_withComplexObject() {
        AtomicReference<String> result = new AtomicReference<>();
        TestObject testObject = new TestObject("test-name", 42);

        Objects.whenNotNull(testObject, obj -> result.set(obj.getName() + "-" + obj.getValue()));

        assertEquals("test-name-42", result.get());
    }

    @Test
    void testWhenNotNull_withEmptyString() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);

        Objects.whenNotNull("", value -> consumerCalled.set(true));

        assertTrue(consumerCalled.get());
    }

    @Test
    void testWhenTrue_withNullValue_conditionHandlesNull() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);

        Objects.whenTrue(null, java.util.Objects::isNull, value -> consumerCalled.set(true));

        assertTrue(consumerCalled.get());
    }

    @AllArgsConstructor
    @Getter
    private static class TestObject {
        private final String name;
        private final int value;
    }
}