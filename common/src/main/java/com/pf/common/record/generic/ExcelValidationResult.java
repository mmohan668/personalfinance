package com.pf.common.record.generic;

import java.util.List;
import java.util.Map;

public record ExcelValidationResult(
        Map<Integer, List<String>> errors,
        int nonEmptyRowsCount
) {
}
