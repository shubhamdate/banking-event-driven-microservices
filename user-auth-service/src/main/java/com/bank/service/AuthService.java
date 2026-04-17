package com.bank.service;

import com.bank.dto.*;
import com.bank.entity.User;
import com.bank.exceptions.AuthException;
import com.bank.repository.UserRepository;
import com.bank.security.JwtTokenFactory;
import com.bank.security.PasswordEncoder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;

@ApplicationScoped
public class AuthService {

    private static final String USER_NOT_FOUND = "User Not Found";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenFactory jwtTokenFactory;

    JsonWebToken jwt;

    private static final String INVALID = "Invalid username or password";

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenFactory jwtTokenFactory, JsonWebToken jwt) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenFactory = jwtTokenFactory;
        this.jwt = jwt;
    }

    @Transactional
    public ApiResponse register(RegisterRequest request) throws AuthException {

            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new AuthException(
                        AuthError.builder()
                                .codigoError("1000")
                                .httpStatusCode(400)
                                .descriptionError("User already exists")
                                .build()
                );
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPasswordHash(passwordEncoder.hash(request.getPassword()));
            user.setRole(request.getRole());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setActive(true);
            user.setEmail(request.getEmail());
            user.setMobile(request.getMobile());
            user.setCreatedBy("SYSTEM");

            userRepository.persist(user);

            return ApiResponse.success(toDTO(user), "User Created Successfully");
    }

    public ApiResponse login(LoginRequest request) throws AuthException {

            Optional<User> user = userRepository.findByUsername(request.getUsername());

            if (user.isEmpty() || !user.get().isActive()) {
                throw new AuthException(
                        AuthError.builder()
                                .codigoError("1002")
                                .httpStatusCode(403)
                                .descriptionError(INVALID)
                                .build()
                );
            }

            if (!passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
                throw new AuthException(
                        AuthError.builder()
                                .codigoError("1003")
                                .httpStatusCode(403)
                                .descriptionError(INVALID)
                                .build()
                );
            }

            String token =  jwtTokenFactory.generateToken(user.get().getUsername(), user.get().getId(), String.valueOf(user.get().getRole()), user.get().getEmail(), user.get().getMobile());

            Map<String, String> map = new HashMap<>();

            map.put("accessToken", token);

            return ApiResponse.success(map,"User Authenticated Successfully");
    }

    @Transactional
    public ApiResponse changeUserStatus(Long id, boolean active) throws AuthException {

            User user = userRepository.findById(id);

            if (user == null) {
                throw new AuthException(
                        AuthError.builder()
                                .httpStatusCode(404)
                                .descriptionError(USER_NOT_FOUND)
                                .codigoError("1005")
                                .build()
                );
            }

            if (active && user.isActive()) {
                throw new AuthException(
                        AuthError.builder()
                                .httpStatusCode(404)
                                .descriptionError("User already active")
                                .codigoError("1006")
                                .build()
                );
            }

            if (!active && !user.isActive()) {
                throw new AuthException(
                        AuthError.builder()
                                .httpStatusCode(404)
                                .descriptionError("User already Inactive")
                                .codigoError("1007")
                                .build()
                );
            }

            user.setActive(active);
            userRepository.persist(user);

            return ApiResponse.success(null, "Data Updated successfully");
    }

    @Transactional
    public ApiResponse changePassword(ChangePasswordRequest request) throws AuthException{

            String id = jwt.getSubject();

            User user = userRepository.findById(Long.valueOf(id));

            if (user == null) {
                throw new AuthException(
                        AuthError.builder()
                                .descriptionError(USER_NOT_FOUND)
                                .httpStatusCode(404)
                                .codigoError("1009")
                                .build()
                );
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new AuthException(
                        AuthError.builder()
                                .descriptionError(INVALID)
                                .httpStatusCode(401)
                                .codigoError("1010")
                                .build()
                );
            }

            user.setPasswordHash(passwordEncoder.hash(request.getNewPassword()));
            userRepository.persist(user);

            return ApiResponse.success(new ArrayList<>(), "Password change successfully");
    }

    public ApiResponse getProfile() throws AuthException {
            String userId = jwt.getSubject();

            User user = userRepository.findById(Long.valueOf(userId));

            if (user == null) {
                throw new AuthException(
                        AuthError.builder()
                                .descriptionError(USER_NOT_FOUND)
                                .httpStatusCode(404)
                                .codigoError("1013")
                                .build()
                );
            }

            return ApiResponse.success(toDTO(user), "Request successfully");
    }

    public ApiResponse refreshToken () throws AuthException {
        String userId = jwt.getSubject();
        User user = userRepository.findById(Long.valueOf(userId));

        if (user == null || !user.isActive()) {
                throw new AuthException(
                        AuthError.builder()
                                .descriptionError(USER_NOT_FOUND)
                                .httpStatusCode(404)
                                .codigoError("1013")
                                .build()
                );
        }

        String token =  jwtTokenFactory.generateToken(user.getUsername(), Long.valueOf(userId), String.valueOf(user.getRole()), user.getEmail(), user.getMobile());

        Map<String, String> map = new HashMap<>();

        map.put("accessToken", token);

        return ApiResponse.success(map,"User Authenticated Successfully");
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .active(user.isActive())
                .build();
    }
}