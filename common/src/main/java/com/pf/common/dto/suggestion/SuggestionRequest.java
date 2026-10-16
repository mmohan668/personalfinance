package com.pf.common.dto.suggestion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SuggestionRequest {
    private String entity;
    private String field;
    private String value;
    private String dependentField;
    private String dependentValue;
    private Integer limit;
}
