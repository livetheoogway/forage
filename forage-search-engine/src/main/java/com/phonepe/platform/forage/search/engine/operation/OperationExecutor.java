package com.phonepe.platform.forage.search.engine.operation;

import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.model.opresult.FailedOperation;
import com.phonepe.platform.forage.search.engine.model.opresult.FailedResult;
import com.phonepe.platform.forage.search.engine.model.opresult.OperationResult;
import com.phonepe.platform.forage.search.engine.model.opresult.PartiallyFailedResult;
import com.phonepe.platform.forage.search.engine.model.opresult.SuccessResult;
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
                failedOperations = consumeAndCheckFailure(consumer, failedOperations, item);
            }
            if (failedOperations != null) {
                return new PartiallyFailedResult<>(failedOperations);
            }
        } catch (Exception e) {
            return new FailedResult<>(ForageSearchError.propagate(e));
        }
        return new SuccessResult<>();
    }

    private <S> List<FailedOperation<S>> consumeAndCheckFailure(final EConsumer<S> consumer,
                                                                List<FailedOperation<S>> failedOperations,
                                                                final S item) throws Exception {
        try {
            consumer.accept(item);
        } catch (ForageSearchError e) {
            /* we only consider forage errors as partial failures, else it is a FailedResult */
            if (failedOperations == null) {
                failedOperations = new ArrayList<>();
            }
            failedOperations.add(new FailedOperation<>(item, e));
        }
        return failedOperations;
    }
}
