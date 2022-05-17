package com.phonepe.platform.forage.search.engine.lucene;

import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import com.phonepe.platform.forage.search.engine.lucene.parser.QueryParserSupplier;
import com.phonepe.platform.forage.models.query.search.BooleanQuery;
import com.phonepe.platform.forage.models.query.search.ClauseVisitor;
import com.phonepe.platform.forage.models.query.search.FuzzyMatchQuery;
import com.phonepe.platform.forage.models.query.search.MatchQuery;
import com.phonepe.platform.forage.models.query.search.ParsableQuery;
import com.phonepe.platform.forage.models.query.search.QueryVisitor;
import com.phonepe.platform.forage.models.query.search.RangeQuery;
import com.phonepe.platform.forage.models.query.search.range.FloatRange;
import com.phonepe.platform.forage.models.query.search.range.IntRange;
import com.phonepe.platform.forage.models.query.search.range.RangeVisitor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

@AllArgsConstructor
public class LuceneQueryGenerator implements QueryVisitor<Query> {
    private static final ClauseVisitor<BooleanClause.Occur> CLAUSE_VISITOR = new ClauseVisitor<>() {
        @Override
        public BooleanClause.Occur must() {
            return BooleanClause.Occur.MUST;
        }

        @Override
        public BooleanClause.Occur should() {
            return BooleanClause.Occur.SHOULD;
        }

        @Override
        public BooleanClause.Occur mustNot() {
            return BooleanClause.Occur.MUST_NOT;
        }

        @Override
        public BooleanClause.Occur filter() {
            return BooleanClause.Occur.FILTER;
        }
    };
    private QueryParserSupplier queryParserSupplier;

    @Override
    public Query visit(final BooleanQuery booleanQuery) {
        final org.apache.lucene.search.BooleanQuery.Builder queryBuilder =
                new org.apache.lucene.search.BooleanQuery.Builder();
        booleanQuery.getQueries()
                .stream()
                .map(this::visitThis)
                .forEach(query -> queryBuilder.add(query, booleanQuery.getClauseType().accept(CLAUSE_VISITOR)));
        return queryBuilder.build();
    }

    @Override
    public Query visit(final MatchQuery matchQuery) {
        return new TermQuery(new Term(matchQuery.getField(), matchQuery.getValue()));
    }

    @Override
    public Query visit(final ParsableQuery parsableQuery) throws ForageSearchError {
        try {
            return queryParserSupplier
                    .queryParser(parsableQuery.getField())
                    .parse(parsableQuery.getQueryString());
        } catch (Exception e) {
            throw new ForageSearchError(ForageErrorCode.QUERY_PARSE_ERROR, e);
        }
    }

    @Override
    public Query visit(final RangeQuery rangeQuery) {
        return rangeQuery.getRange().accept(new RangeVisitor<>() {
            @Override
            public Query visit(final IntRange intRange) {
                return IntPoint.newRangeQuery(rangeQuery.getField(), intRange.getLow(), intRange.getHigh());
            }

            @Override
            public Query visit(final FloatRange floatRange) {
                return FloatPoint.newRangeQuery(rangeQuery.getField(), floatRange.getLow(), floatRange.getHigh());
            }
        });
    }

    @Override
    public Query visit(final FuzzyMatchQuery fuzzyMatchQuery) {
        return new FuzzyQuery(new Term(fuzzyMatchQuery.getField(), fuzzyMatchQuery.getValue()));
    }

    @SneakyThrows
    private Query visitThis(final com.phonepe.platform.forage.models.query.search.Query query) {
        return query.accept(this);
    }
}
