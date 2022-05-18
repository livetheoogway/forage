package com.livetheoogway.forage.search.engine.model.opresult;

public interface OperationResultVisitor<S, T> {
    T visit(PartiallyFailedResult<S> success);

    T visit(SuccessResult<S> successResult);

    T visit(FailedResult<S> failedResult);
}
