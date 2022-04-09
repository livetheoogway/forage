package com.phonepe.platform.forage.search.engine.operation;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.result.FailedOperation;
import com.phonepe.platform.forage.search.engine.model.result.FailedResult;
import com.phonepe.platform.forage.search.engine.model.result.OperationResult;
import com.phonepe.platform.forage.search.engine.model.result.PartiallyFailedResult;
import com.phonepe.platform.forage.search.engine.model.result.SuccessResult;
import com.phonepe.platform.forage.search.engine.util.EConsumer;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class OperationExecutor {

    public <S> OperationResult<S> execute(final List<S> items, final EConsumer<S> consumer) {
        try {
            List<FailedOperation<S>> failedOperations = null;
            for (val item : items) {
                try {
                    consumer.accept(item);
                } catch (Exception e) {
                    if (failedOperations == null) {
                        failedOperations = new ArrayList<>();
                    }
                    failedOperations.add(new FailedOperation<>(item, ForageSearchError.propagate(e)));
                }
            }
            if (failedOperations != null) {
                return new PartiallyFailedResult<>(failedOperations);
            }
        } catch (Exception e) {
            return new FailedResult<>(ForageSearchError.propagate(e));
        }
        return new SuccessResult<>();
    }
}
