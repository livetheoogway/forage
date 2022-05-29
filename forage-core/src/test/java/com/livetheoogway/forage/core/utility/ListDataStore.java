package com.livetheoogway.forage.core.utility;

import com.livetheoogway.forage.core.Bootstrapper;
import com.livetheoogway.forage.models.DataId;

import java.util.ArrayList;
import java.util.List;

public abstract class ListDataStore<D extends DataId> implements Bootstrapper<D> {
    protected final List<D> database = new ArrayList<>();

    public void addData(D item) {
        database.add(item);
    }

}
