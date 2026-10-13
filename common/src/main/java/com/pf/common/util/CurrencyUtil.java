package com.pf.common.util;

import lombok.NoArgsConstructor;

import java.util.Currency;
import java.util.Locale;

@NoArgsConstructor
public class CurrencyUtil {

    public static String getCurrencySymbol(String currencyCode, String localeTag) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return "";
        }

        try {
            String code = currencyCode.trim().toUpperCase(Locale.ROOT);
            Currency currency = Currency.getInstance(code);

            if (localeTag != null && !localeTag.trim().isEmpty()) {
                Locale locale = Locale.forLanguageTag(localeTag.trim());
                return currency.getSymbol(locale);
            }

            // Fallback when locale is not provided
            return currency.getSymbol(Locale.ROOT);

        } catch (IllegalArgumentException e) {
            return currencyCode;
        }
    }

}
