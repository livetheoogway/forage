/*
 * Copyright 2022. Live the Oogway, Tushar Naik
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

import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionWrappedExecutorTest {
    @Test
    void testThatExceptionWrappedExecutorDoesItsJob() {
        assertThrows(ForageSearchError.class, () -> ExceptionWrappedExecutor.get(() -> {
            throw ForageSearchError.raise(ForageErrorCode.SOMETHING_WENT_WRONG, "");
        }, ForageErrorCode.SOMETHING_WENT_WRONG));
        assertThrows(ForageSearchError.class, () -> ExceptionWrappedExecutor.get(() -> {
            throw new RuntimeException();
        }, ForageErrorCode.SOMETHING_WENT_WRONG));
    }
}