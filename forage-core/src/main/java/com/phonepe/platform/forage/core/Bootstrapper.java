package com.phonepe.platform.forage.core;


import com.phonepe.platform.forage.models.StoredData;

/**
 * @param <D> Actual data item
 * @param <S> Stored data
 */
public interface Bootstrapper<D, S extends StoredData<D>> {
    void bootstrap(UpdateConsumer<S> updateConsumer);
}
