package com.phonepe.platform.forage.search.engine.exception;

import lombok.Getter;

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
        if (cause instanceof ForageSearchError) {
            return (ForageSearchError) cause;
        }
        return new ForageSearchError(ForageErrorCode.SOMETHING_WENT_WRONG, cause);
    }
}
