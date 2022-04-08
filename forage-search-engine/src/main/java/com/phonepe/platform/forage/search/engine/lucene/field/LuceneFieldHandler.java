package com.phonepe.platform.forage.search.engine.lucene.field;

import com.phonepe.platform.forage.search.engine.model.field.FieldVisitor;
import com.phonepe.platform.forage.search.engine.model.field.LuceneField;
import com.phonepe.platform.forage.search.engine.model.field.StringField;
import com.phonepe.platform.forage.search.engine.model.field.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

public class LuceneFieldHandler implements FieldVisitor<IndexableField> {

//    private static final String FORAGE_FIELD_TYPE = "forageFieldType";
//
//    static {
//        /* this is being done for ensuring seamless field conversion from lucene doc fields to forage fields */
////        org.apache.lucene.document.StringField.TYPE_NOT_STORED.putAttribute(FORAGE_FIELD_TYPE, FieldType.STRING
// .name());
////        org.apache.lucene.document.TextField.TYPE_NOT_STORED.putAttribute(FORAGE_FIELD_TYPE, FieldType.STRING
// .name());
//    }

    @Override
    public IndexableField visit(final TextField textField) {
        return new org.apache.lucene.document.TextField(
                textField.getName(),
                textField.getValue(),
                Field.Store.NO);
    }

    @Override
    public IndexableField visit(final StringField stringField) {
        return new org.apache.lucene.document.StringField(
                stringField.getName(),
                stringField.getValue(),
                Field.Store.NO);
    }

    @Override
    public IndexableField visit(final LuceneField luceneField) {
        return luceneField.getField();
    }
//
//    public static Field extractField(
//            final IndexableField indexableField) {
//        final Optional<FieldType> fieldType = LuceneFieldHandler.identifyFieldType(indexableField);
//        if (fieldType.isPresent()) {
//            return fieldType.get().accept(
//                    new FieldTypeVisitor<>() {
//                        @Override
//                        public Field text() {
//                            return new TextField(indexableField.name(), indexableField.stringValue());
//                        }
//
//                        @Override
//                        public Field string() {
//                            return new StringField(indexableField.name(), indexableField.stringValue());
//                        }
//
//                        @Override
//                        public Field lucene() {
//                            return new LuceneField(indexableField);
//                        }
//                    });
//        }
//        return new LuceneField(indexableField);
//    }
//
//    public static Optional<FieldType> identifyFieldType(IndexableField indexableField) {
//        final String fieldTypeHint = indexableField.fieldType().getAttributes().get(FORAGE_FIELD_TYPE);
//        if (fieldTypeHint.isBlank()) {
//            return Optional.empty();
//        }
//        try {
//            return Optional.of(FieldType.valueOf(fieldTypeHint));
//        } catch (Exception ignored) {
//            // todo check if this needs to be ignored
//            return Optional.empty();
//        }
//    }
}
