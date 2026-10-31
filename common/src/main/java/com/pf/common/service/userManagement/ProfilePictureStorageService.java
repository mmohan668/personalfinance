package com.pf.common.service.userManagement;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ProfilePictureStorageService {

    private Path uploadDirectory;

    @Value("${files.images.max-file-size}")
    private DataSize maxFileSize;

    @Value("${files.images.user-profiles-path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        this.uploadDirectory = Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize();
    }

    public String saveImage(String username, MultipartFile file)
            throws IOException {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image.");
        }

        String extension = getExtension(file);

        // Create a separate directory for this user.
        Path userDirectory = uploadDirectory
                .resolve(username)
                .normalize();

        // Prevent the username from escaping the base upload directory.
        if (!userDirectory.startsWith(uploadDirectory)
                || userDirectory.equals(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid username or file path.");
        }

        // Create the user's folder if it does not exist.
        Files.createDirectories(userDirectory);

        // Delete existing profile images for this user.
        try (Stream<Path> files = Files.list(userDirectory)) {
            files.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(
                                    "Failed to delete existing profile image.", e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        // Generate a unique filename.
        String filename = UUID.randomUUID() + extension;

        Path destination = userDirectory.resolve(filename).normalize();

        if (!destination.startsWith(userDirectory)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        // Save the uploaded image.
        file.transferTo(destination);

        // Return only the filename for database storage.
        return filename;
    }

    private String getExtension(MultipartFile file) {

        if (file.getSize() > maxFileSize.toBytes()) {
            throw new IllegalArgumentException(
                    "Image size must not exceed " + maxFileSize + ".");
        }

        String contentType = file.getContentType();

        return switch (contentType == null ? "" : contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Only JPEG, PNG, and WebP images are allowed.");
        };
    }
}