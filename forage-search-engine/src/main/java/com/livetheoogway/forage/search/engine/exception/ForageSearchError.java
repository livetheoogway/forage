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

package com.livetheoogway.forage.search.engine.exception;

import lombok.Getter;

/**
 * A wrapper error class for all internal Exceptions seen in the library
 */
public class ForageSearchError extends Exception {
    @Getter
    private final ForageErrorCode forageErrorCode;

    public ForageSearchError(final ForageErrorCode forageErrorCode,
                             final String message,
                             final Throwable cause) {
        super(message, cause);
        this.forageErrorCode = forageErrorCode;
    }

    public ForageSearchError(final ForageErrorCode forageErrorCode,
                             final String message) {
        super(message);
        this.forageErrorCode = forageErrorCode;
    }

    public ForageSearchError(final ForageErrorCode forageErrorCode,
                             final Throwable cause) {
        super(cause);
        this.forageErrorCode = forageErrorCode;
    }

    public static ForageSearchError raise(final ForageErrorCode forageErrorCode, final String message) {
        return new ForageSearchError(forageErrorCode, message);
    }

    public static ForageSearchError raise(final ForageErrorCode forageErrorCode, final Throwable cause) {
        return new ForageSearchError(forageErrorCode, cause);
    }

    public static ForageSearchError raise(final ForageErrorCode forageErrorCode, final String message,
                                          final Throwable cause) {
        return new ForageSearchError(forageErrorCode, message, cause);
    }

    public static ForageSearchError propagate(final Throwable cause) {
        return propagate(ForageErrorCode.SOMETHING_WENT_WRONG, cause);
    }

    public static ForageSearchError propagate(ForageErrorCode errorCode, final Throwable cause) {
        if (cause instanceof ForageSearchError) {
            return (ForageSearchError) cause;
        }
        return new ForageSearchError(errorCode, cause);
    }
}
