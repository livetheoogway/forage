package com.phonepe.platform.forage.search.engine.model.result;

public interface OperationResultVisitor<S, T> {
    T visit(PartiallyFailedResult<S> success);

    T visit(SuccessResult<S> successResult);

    T visit(FailedResult<S> failedResult);
}
