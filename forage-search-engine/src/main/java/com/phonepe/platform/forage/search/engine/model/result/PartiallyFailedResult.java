package com.phonepe.platform.forage.search.engine.model.result;

import lombok.Getter;

import java.util.List;

public class PartiallyFailedResult<S> extends OperationResult<S> {
    /* number of successful */
    @Getter
    private final List<FailedOperation<S>> failed;

    public PartiallyFailedResult(final List<FailedOperation<S>> failed) {
        this.failed = failed;
    }

    @Override
    public <T> T accept(final OperationResultVisitor<S, T> visitor) {
        return visitor.visit(this);
    }
}
