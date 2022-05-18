package com.livetheoogway.forage.search.engine.lucene.field;

import com.livetheoogway.forage.models.result.field.FieldVisitor;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.StringField;
import com.livetheoogway.forage.models.result.field.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

public class LuceneFieldHandler implements FieldVisitor<IndexableField> {

    @Override
    public IndexableField visit(final TextField textField) {
        return new org.apache.lucene.document.TextField(
                textField.getName(),
                textField.getValue(),
                Field.Store.NO);
    }

    @Override
    public IndexableField visit(final StringField stringField) {
        return new org.apache.lucene.document.StringField(
                stringField.getName(),
                stringField.getValue(),
                Field.Store.NO);
    }

    @Override
    public IndexableField visit(final FloatField floatField) {
        return new org.apache.lucene.document.FloatPoint(
                floatField.getName(),
                floatField.getPoints());
    }

    @Override
    public IndexableField visit(final IntField intField) {
        return new org.apache.lucene.document.IntPoint(
                intField.getName(),
                intField.getPoints());
    }
}
