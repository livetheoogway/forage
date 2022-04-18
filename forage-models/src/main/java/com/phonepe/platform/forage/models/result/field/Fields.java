package com.phonepe.platform.forage.models.result.field;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Fields {
    public static Field stringField(String name, String value) {
        return new StringField(name, value);
    }

    public static Field textField(String name, String value) {
        return new TextField(name, value);
    }
}
