package com.livetheoogway.forage.search.engine.exception;

public enum ForageErrorCode {
    SOMETHING_WENT_WRONG,
    INVALID_STATE,
    INDEX_WRITER_IO_ERROR,
    INDEX_FLUSH_ERROR,
    QUERY_PARSE_ERROR,
    PAGE_GENERATION_ERROR,
    PAGE_PARSE_ERROR,
    INDEX_READER_IO_ERROR,
    SEARCH_ERROR,
    PAGE_QUERY_PARSE_ERROR,
    QUERY_ENGINE_NOT_INITIALIZED_YET
}