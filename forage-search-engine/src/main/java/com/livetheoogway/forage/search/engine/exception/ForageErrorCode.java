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
    QUERY_ENGINE_NOT_INITIALIZED_YET,
    DATASTORE_INVALID,
    SEARCH_ENGINE_INITIALIZATION_ERROR
}
