package com.livetheoogway.forage.core.model;

import com.livetheoogway.forage.models.DataId;
import lombok.Value;

@Value
public class TestDataItem implements DataId {
    String id;
    String message;

    @Override
    public String id() {
        return id;
    }

}
