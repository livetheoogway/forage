package com.phonepe.platform.forage.search.engine.model.result;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import lombok.Getter;

public class FailedResult<S> extends OperationResult<S> {
    @Getter
    private final ForageSearchError searchError;

    public FailedResult(final ForageSearchError searchError) {
        this.searchError = searchError;
    }

    @Override
    public <T> T accept(final OperationResultVisitor<S, T> visitor) {
        return visitor.visit(this);
    }
}
