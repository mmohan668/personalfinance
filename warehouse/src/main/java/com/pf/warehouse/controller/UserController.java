package com.pf.warehouse.controller;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.entity.userManagement.ProfilePicture;
import com.pf.common.repository.user.ProfilePictureRepository;
import com.pf.common.service.userManagement.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProfilePictureRepository profilePictureRepository;

    @Value("${files.images.user-profiles-path}")
    private String uploadPath;

    @PostMapping("/fetchUsersGridData")
    public GridResult fetchUsersGridData(@RequestBody SearchCriteria searchCriteria) {
        return userService.fetchUsersGridData(searchCriteria);
    }

    @PostMapping("/saveUser")
    public ApiResponse saveUser(@RequestBody UserDto userDto) {
        return userService.saveUser(userDto);
    }

    @PostMapping("/activateUsers")
    public ApiResponse activateUsers(@RequestBody List<Long> ids) {
        return userService.activateUsers(ids);
    }

    @PostMapping("/inactivateUsers")
    public ApiResponse inactivateUsers(@RequestBody List<Long> ids) {
        return userService.inactivateUsers(ids);
    }

    @PostMapping("/deleteUsers")
    public ApiResponse deleteUsers(@RequestBody List<Long> ids) {
        return userService.deleteUsers(ids);
    }

    @PostMapping(
            value = "/saveProfilePicture",
            consumes = "multipart/form-data"
    )
    public ApiResponse saveProfilePicture(
            @RequestParam("file") MultipartFile file) {
        return userService.saveProfilePicture(file);
    }

    @GetMapping("/{username}/profile-picture")
    public ResponseEntity<Resource> getProfilePicture(
            @PathVariable String username) throws IOException {

        ProfilePicture picture = profilePictureRepository
                .findByUserUsername(username)
                .orElse(null);

        if (picture == null) {
            return ResponseEntity.notFound().build();
        }

        Path userDirectory = Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize()
                .resolve(username)
                .normalize();

        if (!userDirectory.startsWith(
                Paths.get(uploadPath).toAbsolutePath().normalize())) {
            return ResponseEntity.badRequest().build();
        }

        Path imagePath = userDirectory
                .resolve(picture.getImagePath())
                .normalize();

        if (!imagePath.startsWith(userDirectory)
                || !Files.isRegularFile(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(imagePath.toUri());

        MediaType contentType = MediaType.parseMediaType(
                picture.getContentType()
        );

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

}
