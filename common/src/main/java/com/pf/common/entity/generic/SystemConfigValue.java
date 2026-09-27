package com.pf.common.entity.generic;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigValue implements Serializable {
    private String currencyCode;
    private String locale;
    private String dateFormat;
}
