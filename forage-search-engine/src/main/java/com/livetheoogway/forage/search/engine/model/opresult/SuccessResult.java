package com.livetheoogway.forage.search.engine.model.opresult;

public class SuccessResult<S> implements OperationResult<S> {

    @Override
    public <T> T accept(final OperationResultVisitor<S, T> visitor) {
        return visitor.visit(this);
    }
}
