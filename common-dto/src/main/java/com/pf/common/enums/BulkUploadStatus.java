package com.pf.common.enums;

import lombok.Getter;

@Getter
public enum BulkUploadStatus {

    RECEIVED(
            "Received",
            "File has been uploaded and accepted, but processing hasn't started"
    ),

    IN_PROCESS(
            "In Process",
            "File is currently being processed"
    ),

    PARTIAL_SUCCESS(
            "Partial Success",
            "Processing completed, but some records succeeded and some failed due to validation errors"
    ),

    ERRORS(
            "Errors",
            "Processing could not complete due to validation errors"
    ),

    NO_DATA(
            "No Data",
            "Processing could not complete due no data"
    ),

    FAILED(
            "Failed",
            "Processing could not complete due to exceptions"
    ),

    SUCCESS(
            "Success",
            "All records processed successfully"
    );

    private final String value;
    private final String description;

    BulkUploadStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
