package com.phonepe.platform.forage.search.engine.model.field;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.apache.lucene.index.IndexableField;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LuceneField extends Field {
    IndexableField field;

    public LuceneField(final IndexableField field) {
        super(FieldType.LUCENE);
        this.field = field;
    }

    @Override
    public <T> T accept(final FieldVisitor<T> fieldVisitor) {
        return fieldVisitor.visit(this);
    }
}
