package com.phonepe.platform.forage.core;


public abstract class Bootstrapper<K, D, U, C extends UpdateConsumer<U>> {
    private PersistentStore<K, D, U, C> persistentStore;
    private C updateConsumer;

    public void bootstrap() throws BootstrapException {
        persistentStore.bootstrap(updateConsumer);
    }

    abstract void start();
}
