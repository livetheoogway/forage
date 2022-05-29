package com.livetheoogway.forage.search.engine.model.index;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.livetheoogway.forage.models.DataId;
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
public abstract class IndexableDocument implements DataId {
    @Getter
    @NotNull
    private DocumentType type;

    @Getter
    @NotNull
    private String id;

    @Override
    public String id() {
        return id;
    }

    public abstract <T> T accept(DocumentVisitor<T> visitor);
}
