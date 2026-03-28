package ims.aefryyu.server.controller;

import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.UserDTO;
import ims.aefryyu.server.entity.User;
import ims.aefryyu.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(){
        return ResponseEntity.ok(userService.getCurrentLoggedUser());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateUser(@PathVariable UUID id,@RequestBody UserDTO userDTO){
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable UUID id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/all-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getUserActive(){
        return ResponseEntity.ok(userService.getAllUsersActive());
    }

    @GetMapping("/all-inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getUserInActive(){
        return ResponseEntity.ok(userService.getAllUsersInactive());
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<Response> getTransactionUserId(@PathVariable UUID userId){
        return ResponseEntity.ok(userService.getUserTransactions(userId));
    }
}
