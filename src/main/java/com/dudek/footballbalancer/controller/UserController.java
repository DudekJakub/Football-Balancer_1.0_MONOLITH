package com.dudek.footballbalancer.controller;

import com.dudek.footballbalancer.model.dto.user.UserSimpleDto;
import com.dudek.footballbalancer.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "This API provides all operations about user (except auth operations).")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public ResponseEntity<UserSimpleDto> userInfo(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(userService.userInfo(userId));
    }
}
