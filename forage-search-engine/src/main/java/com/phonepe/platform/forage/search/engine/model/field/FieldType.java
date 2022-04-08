package com.phonepe.platform.forage.search.engine.model.field;

public enum FieldType {
    TEXT {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.text();
        }
    },
    STRING{
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.string();
        }
    }, /* analysis disabled */
    LUCENE {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.lucene();
        }
    };
    public abstract <T> T accept(FieldTypeVisitor<T> visitor);
}