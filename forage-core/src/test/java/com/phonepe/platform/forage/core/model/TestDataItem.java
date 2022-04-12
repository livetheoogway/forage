package com.phonepe.platform.forage.core.model;

import com.phonepe.platform.forage.models.StoredData;
import lombok.Value;

@Value
public class TestDataItem implements StoredData<String> {
    String id;
    String message;

    @Override
    public String id() {
        return id;
    }

    @Override
    public String data() {
        return message;
    }
}
