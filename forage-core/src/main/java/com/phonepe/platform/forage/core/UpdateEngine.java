package com.phonepe.platform.forage.core;

import com.phonepe.platform.forage.models.StoredData;

public abstract class UpdateEngine<T, D extends StoredData<T>> {
    private final Bootstrapper<T, D> bootstrapper;
    private final UpdateConsumer<D> updateConsumer;

    protected UpdateEngine(final Bootstrapper<T, D> bootstrapper, final UpdateConsumer<D> updateConsumer) {
        this.bootstrapper = bootstrapper;
        this.updateConsumer = updateConsumer;
    }

    public void bootstrap() {
        bootstrapper.bootstrap(updateConsumer);
    }

    public abstract void start();
    public abstract void stop();
}
