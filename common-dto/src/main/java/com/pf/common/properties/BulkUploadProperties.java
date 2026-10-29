package com.pf.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "bulk-upload.base-folder")
@Component
@Getter
@Setter
public class BulkUploadProperties {
    private String inProgress;
    private String success;
    private String error;
}
