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

package com.livetheoogway.forage.models.result.field;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldBoostTest {

    @Test
    void testTextFieldWithBoost() {
        TextField field = new TextField("title", "Effective Java", 2.0f);
        
        assertEquals("title", field.getName());
        assertEquals("Effective Java", field.getValue());
        assertEquals(2.0f, field.getBoost());
        assertEquals(FieldType.TEXT, field.getFieldType());
    }

    @Test
    void testTextFieldWithoutBoost() {
        TextField field = new TextField("title", "Effective Java");
        
        assertEquals("title", field.getName());
        assertEquals("Effective Java", field.getValue());
        assertNull(field.getBoost());
        assertEquals(FieldType.TEXT, field.getFieldType());
    }

    @Test
    void testStringFieldWithBoost() {
        StringField field = new StringField("category", "programming", 1.5f);
        
        assertEquals("category", field.getName());
        assertEquals("programming", field.getValue());
        assertEquals(1.5f, field.getBoost());
        assertEquals(FieldType.STRING, field.getFieldType());
    }

    @Test
    void testStringFieldWithoutBoost() {
        StringField field = new StringField("category", "programming");
        
        assertEquals("category", field.getName());
        assertEquals("programming", field.getValue());
        assertNull(field.getBoost());
        assertEquals(FieldType.STRING, field.getFieldType());
    }

    @Test
    void testIntFieldWithBoost() {
        int[] values = {100, 200, 300};
        IntField field = new IntField("pages", values, 1.2f);
        
        assertEquals("pages", field.getName());
        assertArrayEquals(values, field.getPoints());
        assertEquals(1.2f, field.getBoost());
        assertEquals(FieldType.INT, field.getFieldType());
    }

    @Test
    void testIntFieldWithoutBoost() {
        int[] values = {100, 200, 300};
        IntField field = new IntField("pages", values);
        
        assertEquals("pages", field.getName());
        assertArrayEquals(values, field.getPoints());
        assertNull(field.getBoost());
        assertEquals(FieldType.INT, field.getFieldType());
    }

    @Test
    void testFloatFieldWithBoost() {
        float[] values = {4.5f, 3.8f, 4.9f};
        FloatField field = new FloatField("rating", values, 2.5f);
        
        assertEquals("rating", field.getName());
        assertArrayEquals(values, field.getPoints());
        assertEquals(2.5f, field.getBoost());
        assertEquals(FieldType.FLOAT, field.getFieldType());
    }

    @Test
    void testFloatFieldWithoutBoost() {
        float[] values = {4.5f, 3.8f, 4.9f};
        FloatField field = new FloatField("rating", values);
        
        assertEquals("rating", field.getName());
        assertArrayEquals(values, field.getPoints());
        assertNull(field.getBoost());
        assertEquals(FieldType.FLOAT, field.getFieldType());
    }

    @Test
    void testFieldVisitorPatternWithBoost() {
        TextField textField = new TextField("title", "Test Book", 1.5f);
        StringField stringField = new StringField("isbn", "123456789", 1.0f);
        IntField intField = new IntField("pages", new int[]{300}, 0.8f);
        FloatField floatField = new FloatField("rating", new float[]{4.2f}, 2.0f);

        FieldVisitor<String> visitor = new FieldVisitor<String>() {
            @Override
            public String visit(TextField textField) {
                return "TEXT:" + textField.getName() + ":" + textField.getBoost();
            }

            @Override
            public String visit(StringField stringField) {
                return "STRING:" + stringField.getName() + ":" + stringField.getBoost();
            }

            @Override
            public String visit(IntField intField) {
                return "INT:" + intField.getName() + ":" + intField.getBoost();
            }

            @Override
            public String visit(FloatField floatField) {
                return "FLOAT:" + floatField.getName() + ":" + floatField.getBoost();
            }
        };

        assertEquals("TEXT:title:1.5", textField.accept(visitor));
        assertEquals("STRING:isbn:1.0", stringField.accept(visitor));
        assertEquals("INT:pages:0.8", intField.accept(visitor));
        assertEquals("FLOAT:rating:2.0", floatField.accept(visitor));
    }

    @Test
    void testFieldVisitorPatternWithoutBoost() {
        TextField textField = new TextField("title", "Test Book");
        StringField stringField = new StringField("isbn", "123456789");

        FieldVisitor<String> visitor = new FieldVisitor<String>() {
            @Override
            public String visit(TextField textField) {
                return "TEXT:" + textField.getName() + ":" + textField.getBoost();
            }

            @Override
            public String visit(StringField stringField) {
                return "STRING:" + stringField.getName() + ":" + stringField.getBoost();
            }

            @Override
            public String visit(IntField intField) {
                return "INT:" + intField.getName() + ":" + intField.getBoost();
            }

            @Override
            public String visit(FloatField floatField) {
                return "FLOAT:" + floatField.getName() + ":" + floatField.getBoost();
            }
        };

        assertEquals("TEXT:title:null", textField.accept(visitor));
        assertEquals("STRING:isbn:null", stringField.accept(visitor));
    }
}