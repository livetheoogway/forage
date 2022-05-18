package com.livetheoogway.forage.search.engine.model.index;

import com.livetheoogway.forage.models.result.field.Field;
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
public class ForageDocument<D> extends IndexableDocument<D> {

    @NotNull
    @NotEmpty
    List<Field> fields;

    @Builder
    public ForageDocument(final String id, final D data, @Singular final List<Field> fields) {
        super(DocumentType.FORAGE, id, data);
        this.fields = fields;
    }

    @Override
    public <T> T accept(final DocumentVisitor<T, D> visitor) {
        return visitor.visit(this);
    }
}
