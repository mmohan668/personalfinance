package com.pf.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

public class FileUtils {

    public static Path getDateTimePath(String... pathSegments) throws IOException {
        Path path = Paths.get(
                pathSegments[0],
                Arrays.copyOfRange(pathSegments, 1, pathSegments.length)
        ).resolve(getDatePath()).resolve(getTimePath());
        Files.createDirectories(path);
        return path;
    }

    public static String getDatePath() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static String getTimePath() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HHmmss_SSS"));
    }

    public static String safePathSegment(String value) {
        if (value == null || value.isBlank()
                || value.equals(".") || value.equals("..")
                || value.contains("/") || value.contains("\\")) {
            throw new IllegalArgumentException("Invalid path segment");
        }
        return value;
    }

    public static String safeFileName(String fileName) {
        String safeName = safePathSegment(fileName);
        if (!safeName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            safeName += ".xlsx";
        }
        return safeName;
    }

    public static String addTimestamp(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                ? fileName.substring(0, fileName.length() - 5)
                : fileName;

        return name + "_" + System.currentTimeMillis() + ".xlsx";
    }
}
