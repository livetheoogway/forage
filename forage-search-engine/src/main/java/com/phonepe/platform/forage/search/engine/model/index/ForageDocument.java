package com.phonepe.platform.forage.search.engine.model.index;

import com.phonepe.platform.forage.search.engine.model.field.Field;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForageDocument extends IndexableDocument {

    @NotNull
    @NotEmpty
    List<Field> fields;

    @Builder
    public ForageDocument(final String id, final Object data, @Singular final List<Field> fields) {
        super(DocumentType.FORAGE, id, data);
        this.fields = fields;
    }

    @Override
    public <T> T accept(final DocumentVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
