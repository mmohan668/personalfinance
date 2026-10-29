package com.pf.warehouse.controller;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.userManagement.UserDto;
import com.pf.common.service.userManagement.AccountRegistrationService;
import com.pf.common.service.userManagement.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AccountRegistrationService accountRegistrationService;

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

    @PostMapping("/signUp")
    public ApiResponse signUp(@RequestBody UserDto userDto) {
        return accountRegistrationService.signUp(userDto);
    }

    @GetMapping("/activateAccount")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        return ResponseEntity.ok(accountRegistrationService.activateAccount(token));
    }
}
