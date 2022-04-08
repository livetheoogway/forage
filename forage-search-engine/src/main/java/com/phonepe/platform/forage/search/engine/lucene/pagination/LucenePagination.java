package com.phonepe.platform.forage.search.engine.lucene.pagination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.platform.forage.search.engine.Pagination;
import com.phonepe.platform.forage.search.engine.exception.ForageErrorCode;
import com.phonepe.platform.forage.search.engine.exception.ForageSearchError;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Base64;

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
