package com.livetheoogway.forage.search.engine.model.opresult;

public interface OperationResult<S> {
    <T> T accept(OperationResultVisitor<S, T> visitor);
}
