package com.phonepe.platform.forage.search.engine.model.index;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.apache.lucene.document.Document;

import javax.validation.constraints.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LuceneDocument<D> extends IndexableDocument<D> {

    @NotNull
    Document document;

    @Builder
    public LuceneDocument(final String id, final D data, final Document document) {
        super(DocumentType.LUCENE, id, data);
        this.document = document;
    }

    @Override
    public <T> T accept(final DocumentVisitor<T, D> visitor) {
        return visitor.visit(this);
    }
}
