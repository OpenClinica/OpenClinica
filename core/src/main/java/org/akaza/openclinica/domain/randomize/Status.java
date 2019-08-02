package org.akaza.openclinica.domain.randomize;

/**
 * The Status enumeration.
 */
public enum Status {
    REQUEST_RECEIVED, PROCESSING, PROCESSING_ERROR, IMPORT_ERROR, RETRY_PROCESSING_ERROR, RETRY_IMPORT_ERROR, SUCCESS
}
