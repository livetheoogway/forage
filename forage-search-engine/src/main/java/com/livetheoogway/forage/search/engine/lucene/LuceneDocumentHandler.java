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

package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.search.engine.model.index.DocumentVisitor;
import com.livetheoogway.forage.search.engine.lucene.field.LuceneFieldHandler;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.LuceneDocument;
import lombok.val;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

public class LuceneDocumentHandler implements DocumentVisitor<Document> {
    private static final String ID = "__ID__";
    private final LuceneFieldHandler fieldGenerator = new LuceneFieldHandler();

    @Override
    public Document visit(final ForageDocument forageDocument) {
        val document = new Document();
        forageDocument.getFields()
                .stream()
                .map(field -> field.accept(fieldGenerator))
                .forEach(document::add);
        document.add(new StringField(ID, forageDocument.id(), Field.Store.YES));
        return document;
    }

    @Override
    public Document visit(final LuceneDocument luceneDocument) {
        val document = luceneDocument.getDocument();
        document.add(new StringField(ID, luceneDocument.id(), Field.Store.YES));
        return document;
    }

    public String extractId(final Document document) {
        return document.get(ID);
    }

}