package com.phonepe.platform.forage.search.engine.model.field;

public enum FieldType {
    TEXT {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.text();
        }
    },
    STRING {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.string();
        }
    },
    LUCENE {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.lucene();
        }
    },
    FLOAT {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.floatPoint();
        }
    },
    INT {
        @Override
        public <T> T accept(final FieldTypeVisitor<T> visitor) {
            return visitor.intPoint();
        }
    };

    public abstract <T> T accept(FieldTypeVisitor<T> visitor);
}