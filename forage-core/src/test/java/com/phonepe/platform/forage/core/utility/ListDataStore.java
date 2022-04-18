package com.phonepe.platform.forage.core.utility;

import com.phonepe.platform.forage.core.Bootstrapper;
import com.phonepe.platform.forage.models.StoredData;

import java.util.ArrayList;
import java.util.List;

public abstract class ListDataStore<D, S extends StoredData<D>> implements Bootstrapper<D, S> {
    protected final List<S> database = new ArrayList<>();

    public void addData(S item) {
        database.add(item);
    }

}
