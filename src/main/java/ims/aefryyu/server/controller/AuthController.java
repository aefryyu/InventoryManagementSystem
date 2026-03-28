package ims.aefryyu.server.controller;

import ims.aefryyu.server.dto.LoginRequest;
import ims.aefryyu.server.dto.RegisterRequest;
import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    ResponseEntity<Response> register(@RequestBody @Valid RegisterRequest registerRequest){
        return ResponseEntity.ok(userService.userRegister(registerRequest));
    }

    @PostMapping("/login")
    ResponseEntity<Response> login(@RequestBody @Valid LoginRequest loginRequest){
        return ResponseEntity.ok(userService.userLogin(loginRequest));
    }
}
