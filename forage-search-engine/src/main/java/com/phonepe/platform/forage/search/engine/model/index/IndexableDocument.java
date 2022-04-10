package com.phonepe.platform.forage.search.engine.model.index;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.platform.forage.models.StoredData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "FORAGE", value = ForageDocument.class),
        @JsonSubTypes.Type(name = "LUCENE", value = LuceneDocument.class)
})
public abstract class IndexableDocument<D> implements StoredData<D> {
    @Getter
    @NotNull
    private DocumentType type;

    @Getter
    @NotNull
    private String id;

    @Getter
    @NotNull
    private D data;

    @Override
    public String id() {
        return id;
    }

    @Override
    public D data() {
        return data;
    }

    public abstract <T> T accept(DocumentVisitor<T, D> visitor);
}
