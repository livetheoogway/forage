package com.phonepe.platform.forage.search.engine.util;

import com.phonepe.platform.forage.search.engine.model.query.result.Relation;
import com.phonepe.platform.forage.search.engine.model.query.result.TotalResults;
import com.phonepe.platform.forage.search.engine.model.query.result.DocScore;
import lombok.experimental.UtilityClass;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TotalHits;

@UtilityClass
public class Converters {
    public DocScore toDocScore(ScoreDoc scoreDoc) {
        return new DocScore(scoreDoc.score, scoreDoc.doc, scoreDoc.shardIndex);
    }

    public TotalResults toTotalResults(TotalHits totalHits) {
        return new TotalResults(totalHits.value, toRelation(totalHits.relation));
    }

    private static Relation toRelation(final TotalHits.Relation relation) {
        if (relation == TotalHits.Relation.EQUAL_TO) {
            return Relation.EQUAL_TO;
        } else if (relation == TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO) {
            return Relation.GREATER_THAN_OR_EQUAL_TO;
        }
        return Relation.EQUAL_TO; // TODO
    }
}
