package com.pf.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
}
