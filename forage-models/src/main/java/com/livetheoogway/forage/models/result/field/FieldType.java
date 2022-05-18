package com.livetheoogway.forage.models.result.field;

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