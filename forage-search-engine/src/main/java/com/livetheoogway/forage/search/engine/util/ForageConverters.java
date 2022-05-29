/*
 * Copyright 2022. Live the Oogway, Tushar Naik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.livetheoogway.forage.search.engine.util;

import com.livetheoogway.forage.models.result.Relation;
import com.livetheoogway.forage.models.result.TotalResults;
import com.livetheoogway.forage.models.result.DocScore;
import lombok.experimental.UtilityClass;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TotalHits;

@UtilityClass
public class ForageConverters {
    public DocScore toDocScore(ScoreDoc scoreDoc) {
        return new DocScore(scoreDoc.score, scoreDoc.doc, scoreDoc.shardIndex);
    }
    public ScoreDoc toScoreDoc(DocScore docScore) {
        return new ScoreDoc(docScore.getDoc(), docScore.getScore(), docScore.getShardIndex());
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
        return Relation.EQUAL_TO;
    }
}
