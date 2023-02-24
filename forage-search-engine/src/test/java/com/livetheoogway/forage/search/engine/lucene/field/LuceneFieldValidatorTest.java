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

import com.livetheoogway.forage.models.result.field.FloatField;
import com.livetheoogway.forage.models.result.field.IntField;
import com.livetheoogway.forage.models.result.field.StringField;
import com.livetheoogway.forage.models.result.field.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuceneFieldValidatorTest {
    @Test
    void testCorrectFieldValidation() {
        final LuceneFieldValidator luceneFieldValidator = new LuceneFieldValidator();

        assertFalse(new TextField("", null).accept(luceneFieldValidator));
        assertFalse(new TextField("name", null).accept(luceneFieldValidator));
        assertTrue(new TextField("name", "value").accept(luceneFieldValidator));

        assertFalse(new StringField("", null).accept(luceneFieldValidator));
        assertFalse(new StringField("name", null).accept(luceneFieldValidator));
        assertTrue(new StringField("name", "value").accept(luceneFieldValidator));

        assertFalse(new IntField("", null).accept(luceneFieldValidator));
        assertFalse(new IntField("name", null).accept(luceneFieldValidator));
        assertTrue(new IntField("name", new int[]{1}).accept(luceneFieldValidator));

        assertFalse(new FloatField("", null).accept(luceneFieldValidator));
        assertFalse(new FloatField("name", null).accept(luceneFieldValidator));
        assertTrue(new FloatField("name", new float[]{1.1f}).accept(luceneFieldValidator));
    }
}