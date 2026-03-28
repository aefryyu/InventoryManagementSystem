package ims.aefryyu.server.service.impl;

import ims.aefryyu.server.dto.LoginRequest;
import ims.aefryyu.server.dto.RegisterRequest;
import ims.aefryyu.server.dto.Response;
import ims.aefryyu.server.dto.UserDTO;
import ims.aefryyu.server.entity.User;
import ims.aefryyu.server.enums.UserRole;
import ims.aefryyu.server.exception.InvalidCredentialException;
import ims.aefryyu.server.exception.NotFoundException;
import ims.aefryyu.server.repository.UserRepository;
import ims.aefryyu.server.security.JwtUtil;
import ims.aefryyu.server.service.UserService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final RateLimiterService rateLimiterService;

    @Transactional
    @Override
    public Response userRegister(RegisterRequest registerRequest) {
        UserRole userRole = UserRole.MANAGER;

        if(registerRequest.getRole() != null){
            userRole = registerRequest.getRole();
        }

        User userToSave = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(userRole)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("user created successfully")
                .build();
    }

    @Override
    public Response userLogin(LoginRequest loginRequest) {

        Bucket bucket = rateLimiterService.resolveBucket("login:" + loginRequest.getEmail());

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS,"Too many login attempts. Try again later.");
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User not Found or User has been Deleted"));

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new InvalidCredentialException("Password not matches");
        }

        String token = jwtUtil.generateToken(user.getUsername(), String.valueOf(user.getRole()));

        return Response.builder()
                .status(200)
                .message("login success")
                .role(user.getRole())
                .token(token)
                .expirationTime("")
                .build();
    }

    @Override
    public Response getAllUsers() {

        String userId = getCurrentLoggedUser().getId().toString();
        Bucket bucket = rateLimiterService.resolveBucket("users:" + userId);

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS,"Too many login attempts. Try again later.");
        }
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOs = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());


        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOs)
                .build();
    }

    @Override
    public User getCurrentLoggedUser() {

        String userId = getCurrentLoggedUser().getId().toString();
        Bucket bucket = rateLimiterService.resolveBucket("current:" + userId);

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS,"Too many login attempts. Try again later.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTransactions(null);

        return user;
    }

    @Transactional
    @Override
    public Response updateUser(UUID id, UserDTO userDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("Unauthenticated");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if(userDTO.getUsername() != null) existingUser.setUsername(userDTO.getUsername());
        if(userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if(userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());

        if(userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()){
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);
        return Response.builder()
                .status(200)
                .message("Update user id : " + existingUser.getId() + " successfully")
                .build();
    }

    @Transactional
    @Override
    public Response deleteUser(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
        userRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("User Successfully Deleted")
                .build();
    }

    @Override
    public Response getAllUsersActive() {
        List<User> users = userRepository.findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOs = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());


        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOs)
                .build();
    }

    @Override
    public Response getAllUsersInactive() {
        List<User> users = userRepository.findByDeletedAtIsNotNull(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOs = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());


        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOs)
                .build();
    }

    @Override
    public Response getUserTransactions(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.getTransactions().forEach(tDto -> {
            tDto.setUser(null);
            tDto.setSupplier(null);
        });

        return Response.builder()
                .status(200)
                .message("Transaction id: " + id)
                .user(userDTO)
                .build();
    }

}
