/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.search.engine.lucene.field;

import com.livetheoogway.forage.models.result.field.FieldVisitor;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.StringField;
import com.livetheoogway.forage.models.result.field.TextField;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.IndexableField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class follows a visitor pattern on the internal representation of a Field.
 * It essentially converts a Lucene fields to an {@link IndexableField}
 */
public class LuceneFieldHandler implements FieldVisitor<List<IndexableField>> {

    @Override
    public List<IndexableField> visit(final TextField textField) {
        return Collections.singletonList(new org.apache.lucene.document.TextField(
                textField.getName(),
                textField.getValue(),
                Field.Store.NO));
    }

    @Override
    public List<IndexableField> visit(final StringField stringField) {
        return Collections.singletonList(new org.apache.lucene.document.StringField(
                stringField.getName(),
                stringField.getValue(),
                Field.Store.NO));
    }

    @Override
    public List<IndexableField> visit(final FloatField floatField) {
        List<IndexableField> fields = new ArrayList<>();
        // Add Points for range filtering
        fields.add(new FloatPoint(floatField.getName(), floatField.getPoints()));

        // Add DocValues for sorting/scoring
        if (floatField.getPoints().length > 0) {
            fields.add(new DoubleDocValuesField(floatField.getName(), floatField.getPoints()[0]));
        }
        return fields;
    }

    @Override
    public List<IndexableField> visit(final IntField intField) {
        List<IndexableField> fields = new ArrayList<>();
        // Add Points for range filtering
        fields.add(new IntPoint(intField.getName(), intField.getPoints()));

        // Add DocValues for sorting/scoring
        if (intField.getPoints().length > 0) {
            fields.add(new DoubleDocValuesField(intField.getName(), intField.getPoints()[0]));
        }
        return fields;
    }
}
