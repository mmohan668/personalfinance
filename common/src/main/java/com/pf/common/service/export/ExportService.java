package com.pf.common.service.export;

import com.pf.common.dto.export.ExportFileDto;
import com.pf.common.dto.export.ExportRequest;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.service.activeMQ.ActiveMqProducer;
import com.pf.common.service.generic.BaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.pf.common.constants.CommonConstants.SERVER_SIDE_GRID_EXPORT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService extends BaseService {
    private final ActiveMqProducer activeMqProducer;
    @Value("${export.base-folder}")
    private String exportPath;
    private Path basePath;

    @PostConstruct
    public void init() {
        basePath = Paths.get(exportPath).toAbsolutePath().normalize();
    }

    public ApiResponse exportServer(ExportRequest exportRequest) {
        activeMqProducer.sendMessage(SERVER_SIDE_GRID_EXPORT, exportRequest);
        return success("Export request sent to server. Please check downloads section after some time");
    }

    public GridResult getFilesByUsername()
            throws IOException {
        String username = fetchLoginUsername();
        // Validate the username before using it in a filesystem path.
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Invalid username");
        }
        Path userDirectory = basePath.resolve(username).normalize();
        if (!userDirectory.startsWith(basePath)) {
            log.debug("Invalid user directory {}", userDirectory);
            return gridResult(0, new ArrayList<>());
        }
        if (!Files.isDirectory(userDirectory)) {
            log.debug("User export directory not found {}", userDirectory);
            return gridResult(0, new ArrayList<>());
        }
        List<ExportFileDto> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(userDirectory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".xlsx"))
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attrs =
                                    Files.readAttributes(
                                            path,
                                            BasicFileAttributes.class);
                            Path relativePath =
                                    userDirectory.relativize(path);
                            Path parent = relativePath.getParent();
                            String groupName = parent == null
                                    ? ""
                                    : parent.getFileName().toString();
                            files.add(new ExportFileDto(
                                    groupName,
                                    path.getFileName().toString(),
                                    relativePath.toString(),
                                    attrs.creationTime().toInstant(),
                                    attrs.size()
                            ));
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    });
        } catch (java.io.UncheckedIOException e) {
            throw e.getCause();
        }
        files.sort(
                Comparator.comparing(ExportFileDto::getGroupName)
                        .thenComparing(
                                Comparator.comparing(ExportFileDto::getDateCreated).reversed()
                        )
        );
        return gridResult(files.size(), files);
    }


    public Resource downloadFile(String filePath)
            throws IOException {
        String username = fetchLoginUsername();
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username");
        }
        Path userDirectory = basePath.resolve(username).normalize();
        if (!userDirectory.startsWith(basePath)
                || !Files.isDirectory(userDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory not found");
        }
        // Accept a relative path, never an absolute filesystem path.
        Path resolvedPath = getResolvedPath(filePath, userDirectory);
        // Resolve symlinks too, to prevent escaping the permitted directory.
        Path realRoot = userDirectory.toRealPath();
        Path realFile;
        try {
            realFile = resolvedPath.toRealPath();
        } catch (NoSuchFileException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        if (!realFile.startsWith(realRoot)
                || !Files.isRegularFile(realFile)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        return new FileSystemResource(realFile);
    }

    private static @NonNull Path getResolvedPath(String filePath, Path userDirectory) {
        Path relativePath;
        try {
            relativePath = Path.of(filePath);
        } catch (InvalidPathException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        if (relativePath.isAbsolute()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        Path resolvedPath = userDirectory.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(userDirectory)
                || !resolvedPath.getFileName().toString()
                .toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        return resolvedPath;
    }
}
