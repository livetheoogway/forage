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

package com.livetheoogway.forage.search.engine.lucene.pagination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livetheoogway.forage.search.engine.Pagination;
import com.livetheoogway.forage.search.engine.exception.ForageErrorCode;
import com.livetheoogway.forage.search.engine.exception.ForageSearchError;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Base64;

/**
 * This class is responsible for encrypting the page information from lucene, and getting the same info back
 */
@AllArgsConstructor
public class LucenePagination implements Pagination<SearchAfter, String> {
    public static final SearchAfter DEFAULT_SEARCH_AFTER = new SearchAfter(null, null);
    private final ObjectMapper mapper;

    @Override
    public String generatePage(final SearchAfter searchAfter) throws ForageSearchError {
        try {
            if (searchAfter == null) {
                return null;
            }
            return Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(searchAfter));
        } catch (JsonProcessingException e) {
            throw new ForageSearchError(ForageErrorCode.PAGE_GENERATION_ERROR, e);
        }
    }

    @Override
    public SearchAfter parsePage(final String page) throws ForageSearchError {
        try {
            if (page.isBlank()) {
                return DEFAULT_SEARCH_AFTER;
            }
            return mapper.readValue(Base64.getDecoder().decode(page), SearchAfter.class);
        } catch (IOException e) {
            throw new ForageSearchError(ForageErrorCode.PAGE_PARSE_ERROR, e);
        }
    }
}
