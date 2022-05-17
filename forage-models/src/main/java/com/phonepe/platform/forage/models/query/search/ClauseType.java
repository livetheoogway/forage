package com.phonepe.platform.forage.models.query.search;

public enum ClauseType {
    MUST{
        @Override
        public <T> T accept(final ClauseVisitor<T> clauseVisitor) {
            return clauseVisitor.must();
        }
    },
    SHOULD {
        @Override
        public <T> T accept(final ClauseVisitor<T> clauseVisitor) {
            return clauseVisitor.should();
        }
    },
    MUST_NOT {
        @Override
        public <T> T accept(final ClauseVisitor<T> clauseVisitor) {
            return clauseVisitor.mustNot();
        }
    },
    FILTER {
        @Override
        public <T> T accept(final ClauseVisitor<T> clauseVisitor) {
            return clauseVisitor.filter();
        }
    };

    public abstract <T> T accept(ClauseVisitor<T> clauseVisitor);
}
