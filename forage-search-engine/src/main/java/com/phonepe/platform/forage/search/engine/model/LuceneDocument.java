package com.phonepe.platform.forage.search.engine.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.apache.lucene.document.Document;

import javax.validation.constraints.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LuceneDocument extends IndexableDocument {

    @NotNull
    Document document;

    @Builder
    public LuceneDocument(final String id, final Object data, final Document document) {
        super(DocumentType.LUCENE, id, data);
        this.document = document;
    }

    @Override
    public <T> T accept(final DocumentVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
