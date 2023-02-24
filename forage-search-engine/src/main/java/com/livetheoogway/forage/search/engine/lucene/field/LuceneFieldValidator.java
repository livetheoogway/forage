package com.livetheoogway.forage.search.engine.lucene.field;

import com.google.common.base.Strings;
import com.livetheoogway.forage.models.result.field.FieldVisitor;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.StringField;
import com.livetheoogway.forage.models.result.field.TextField;

public class LuceneFieldValidator implements FieldVisitor<Boolean> {
    @Override
    public Boolean visit(final TextField textField) {
        return !Strings.isNullOrEmpty(textField.getName()) && !Strings.isNullOrEmpty(textField.getValue());
    }

    @Override
    public Boolean visit(final StringField stringField) {
        return !Strings.isNullOrEmpty(stringField.getName()) && !Strings.isNullOrEmpty(stringField.getValue());
    }

    @Override
    public Boolean visit(final FloatField floatField) {
        return !Strings.isNullOrEmpty(floatField.getName()) && floatField.getPoints().length > 0;
    }

    @Override
    public Boolean visit(final IntField intField) {
        return !Strings.isNullOrEmpty(intField.getName()) && intField.getPoints().length > 0;
    }
}
