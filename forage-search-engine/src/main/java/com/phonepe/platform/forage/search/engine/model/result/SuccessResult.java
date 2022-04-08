package com.phonepe.platform.forage.search.engine.model.result;

public class SuccessResult<S> extends OperationResult<S> {

    @Override
    public <T> T accept(final OperationResultVisitor<S, T> visitor) {
        return visitor.visit(this);
    }
}
