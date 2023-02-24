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

import com.google.common.base.Strings;
import com.livetheoogway.forage.models.result.field.FieldVisitor;
import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.StringField;
import com.livetheoogway.forage.models.result.field.TextField;
import lombok.extern.slf4j.Slf4j;

/**
 * Lucene has a bug where, if the value of a field is null, the entire document does not get indexed, without any
 * errors or logs. This Validator silently ignores such fields with a warning
 */
@Slf4j
public class LuceneFieldValidator implements FieldVisitor<Boolean> {

    @Override
    public Boolean visit(final TextField textField) {
        return executeIfFalseAndReturn(
                !Strings.isNullOrEmpty(textField.getName()) && !Strings.isNullOrEmpty(textField.getValue()),
                () -> log.warn("Null values/name for TextField: {}", textField.getName()));
    }

    @Override
    public Boolean visit(final StringField stringField) {
        return executeIfFalseAndReturn(
                !Strings.isNullOrEmpty(stringField.getName()) && !Strings.isNullOrEmpty(stringField.getValue()),
                () -> log.warn("Null values/name for StringField: {}", stringField.getName()));
    }

    @Override
    public Boolean visit(final FloatField floatField) {
        return executeIfFalseAndReturn(
                !Strings.isNullOrEmpty(floatField.getName())
                        && floatField.getPoints() != null
                        && floatField.getPoints().length > 0,
                () -> log.warn("Null values/name for FloatField: {}", floatField.getName()));
    }

    @Override
    public Boolean visit(final IntField intField) {
        return executeIfFalseAndReturn(
                !Strings.isNullOrEmpty(intField.getName())
                        && intField.getPoints() != null
                        && intField.getPoints().length > 0,
                () -> log.warn("Null values/name for IntField: {}", intField.getName()));
    }

    private boolean executeIfFalseAndReturn(final boolean check, final Runnable runnable) {
        if (!check) {
            runnable.run();
        }
        return check;
    }
}
