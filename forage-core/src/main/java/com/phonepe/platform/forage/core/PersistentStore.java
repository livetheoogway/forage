package com.phonepe.platform.forage.core;


public interface PersistentStore<K, D, U, C extends UpdateConsumer<U>> {
    void store(K key, D data);

    void bootstrap(C updateListener) throws BootstrapException;
}
