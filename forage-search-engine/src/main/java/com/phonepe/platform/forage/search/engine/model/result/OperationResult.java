package com.phonepe.platform.forage.search.engine.model.result;

public abstract class OperationResult<S> {
    public abstract <T> T accept(OperationResultVisitor<S, T> visitor);
}
