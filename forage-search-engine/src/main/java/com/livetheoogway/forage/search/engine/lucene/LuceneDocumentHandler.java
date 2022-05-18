package com.livetheoogway.forage.search.engine.lucene;

import com.livetheoogway.forage.search.engine.model.index.DocumentVisitor;
import com.livetheoogway.forage.search.engine.lucene.field.LuceneFieldHandler;
import com.livetheoogway.forage.search.engine.model.index.ForageDocument;
import com.livetheoogway.forage.search.engine.model.index.LuceneDocument;
import lombok.val;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

public class LuceneDocumentHandler<D> implements DocumentVisitor<Document, D> {
    private static final String ID = "__ID__";
    private final LuceneFieldHandler fieldGenerator = new LuceneFieldHandler();

    @Override
    public Document visit(final ForageDocument<D> forageDocument) {
        val document = new Document();
        forageDocument.getFields()
                .stream()
                .map(field -> field.accept(fieldGenerator))
                .forEach(document::add);
        document.add(new StringField(ID, forageDocument.id(), Field.Store.YES));
        return document;
    }

    @Override
    public Document visit(final LuceneDocument<D> luceneDocument) {
        val document = luceneDocument.getDocument();
        document.add(new StringField(ID, luceneDocument.id(), Field.Store.YES));
        return document;
    }

    public String extractId(final Document document) {
        return document.get(ID);
    }

}