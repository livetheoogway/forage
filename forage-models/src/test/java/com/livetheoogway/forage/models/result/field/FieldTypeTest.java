/*
 * Copyright 2026. Live the Oogway, Tushar Naik
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

package com.livetheoogway.forage.models.result.field;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTypeTest {

    private static final FieldTypeVisitor<String> LABELLING_VISITOR = new FieldTypeVisitor<>() {
        @Override
        public String text() {
            return "text";
        }

        @Override
        public String string() {
            return "string";
        }

        @Override
        public String floatPoint() {
            return "float";
        }

        @Override
        public String intPoint() {
            return "int";
        }

        @Override
        public String vector() {
            return "vector";
        }
    };

    @Test
    void testVectorFieldTypeDispatchesToVectorVisitorMethod() {
        assertEquals("vector", FieldType.VECTOR.accept(LABELLING_VISITOR));
    }

    @Test
    void testAllFieldTypesDispatchToCorrectVisitorMethod() {
        assertEquals("text", FieldType.TEXT.accept(LABELLING_VISITOR));
        assertEquals("string", FieldType.STRING.accept(LABELLING_VISITOR));
        assertEquals("float", FieldType.FLOAT.accept(LABELLING_VISITOR));
        assertEquals("int", FieldType.INT.accept(LABELLING_VISITOR));
        assertEquals("vector", FieldType.VECTOR.accept(LABELLING_VISITOR));
    }

    @Test
    void testVectorIsAValidEnumConstant() {
        assertEquals(FieldType.VECTOR, FieldType.valueOf("VECTOR"));
    }
}