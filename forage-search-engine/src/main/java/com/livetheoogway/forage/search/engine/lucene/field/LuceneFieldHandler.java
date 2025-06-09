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
import com.livetheoogway.forage.search.engine.lucene.FieldBoostRegistry;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

/**
 * This class follows a visitor pattern on the internal representation of a Field.
 * It essentially converts a Lucene fields to an {@link IndexableField}
 */
public class LuceneFieldHandler implements FieldVisitor<IndexableField> {
    
    private final FieldBoostRegistry fieldBoostRegistry;
    
    public LuceneFieldHandler() {
        this.fieldBoostRegistry = new FieldBoostRegistry();
    }
    
    public LuceneFieldHandler(FieldBoostRegistry fieldBoostRegistry) {
        this.fieldBoostRegistry = fieldBoostRegistry != null ? fieldBoostRegistry : new FieldBoostRegistry();
    }
    
    public FieldBoostRegistry getFieldBoostRegistry() {
        return fieldBoostRegistry;
    }

    @Override
    public IndexableField visit(final TextField textField) {
        // Register field boost for query-time application (Lucene 9.x approach)
        fieldBoostRegistry.registerFieldBoost(textField.getName(), textField.getBoost());
        
        return new org.apache.lucene.document.TextField(
                textField.getName(),
                textField.getValue(),
                Field.Store.NO);
    }

    @Override
    public IndexableField visit(final StringField stringField) {
        // Register field boost for query-time application (Lucene 9.x approach)
        fieldBoostRegistry.registerFieldBoost(stringField.getName(), stringField.getBoost());
        
        return new org.apache.lucene.document.StringField(
                stringField.getName(),
                stringField.getValue(),
                Field.Store.NO);
    }

    @Override
    public IndexableField visit(final FloatField floatField) {
        // Note: Lucene FloatPoint fields don't support boost during indexing.
        // Field-level boost on numeric fields would need to be implemented at query time.
        // The boost parameter is preserved in the model for potential future use.
        return new org.apache.lucene.document.FloatPoint(
                floatField.getName(),
                floatField.getPoints());
    }

    @Override
    public IndexableField visit(final IntField intField) {
        // Note: Lucene IntPoint fields don't support boost during indexing.
        // Field-level boost on numeric fields would need to be implemented at query time.
        // The boost parameter is preserved in the model for potential future use.
        return new org.apache.lucene.document.IntPoint(
                intField.getName(),
                intField.getPoints());
    }
}
