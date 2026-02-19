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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenFactory jwtTokenFactory;

    JsonWebToken jwt;

    static final String INVALID = "Invalid username or password";

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenFactory jwtTokenFactory, JsonWebToken jwt) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenFactory = jwtTokenFactory;
        this.jwt = jwt;
    }

    @Transactional
    public ApiResponse register(RegisterRequest request) throws AuthException {

        try {
            if (userRepository.findByUsername(request.getUsername()) != null) {
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
            user.setPcCreacion("System");

            userRepository.persist(user);

           UserDTO userDTO =  UserDTO.builder()
                   .id(user.getId())
                   .firstName(user.getFirstName())
                   .lastName(user.getLastName())
                   .username(user.getUsername())
                   .role(user.getRole())
                   .active(user.isActive())
                   .build();

            return ApiResponse.success(List.of(userDTO), "User Created Successfully");
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(
                    AuthError.builder()
                            .httpStatusCode(500)
                            .descriptionError(e.getMessage())
                            .codigoError("1001")
                            .build(), e
            );
        }
    }

    public ApiResponse login(LoginRequest request) throws AuthException {
        try {

            User user = userRepository.findByUsername(request.username);

            if (user == null || !user.isActive()) {
                throw new AuthException(
                        AuthError.builder()
                                .codigoError("1002")
                                .httpStatusCode(403)
                                .descriptionError(INVALID)
                                .build()
                );
            }

            if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
                throw new AuthException(
                        AuthError.builder()
                                .codigoError("1003")
                                .httpStatusCode(403)
                                .descriptionError(INVALID)
                                .build()
                );
            }

            String token =  jwtTokenFactory.generateToken(user.getUsername(), user.getId(), user.getRole());

            Map<String, String> map = new HashMap<>();

            map.put("accessToken", token);

            return ApiResponse.success(map,"User Authenticated Successfully");

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(
                    AuthError.builder()
                            .httpStatusCode(500)
                            .descriptionError(e.getMessage())
                            .codigoError("1004")
                            .build(), e
            );
        }
    }

    @Transactional
    public ApiResponse changeUserStatus(Long id, boolean active) throws AuthException {

        try {
            User user = userRepository.findById(id);

            if (user == null) {
                throw new AuthException(
                        AuthError.builder()
                                .httpStatusCode(404)
                                .descriptionError("User Not Found")
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

            return ApiResponse.success(new ArrayList<>(), "Data Updated successfully");
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(
                    AuthError.builder()
                            .descriptionError(e.getMessage())
                            .codigoError("1008")
                            .httpStatusCode(500)
                            .build()
            );
        }
    }

    @Transactional
    public ApiResponse changePassword(ChangePasswordRequest request) throws AuthException{

        try {

            String id = jwt.getSubject();

            User user = userRepository.findById(Long.valueOf(id));

            if (user == null) {
                throw new AuthException(
                        AuthError.builder()
                                .descriptionError("User Not Found")
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
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(
                    AuthError.builder()
                            .httpStatusCode(500)
                            .codigoError("1011")
                            .descriptionError(e.getMessage())
                            .build(), e
            );
        }
    }

    public ApiResponse getProfile() throws AuthException {
        try {
            String userId = jwt.getSubject();

            User user = userRepository.findById(Long.valueOf(userId));

            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .lastName(user.getLastName())
                    .firstName(user.getFirstName())
                    .role(user.getRole())
                    .active(user.isActive())
                    .build();

            return ApiResponse.success(List.of(userDTO), "Request successfully");

        } catch (Exception e) {
            throw new AuthException(
                    AuthError.builder()
                            .descriptionError(e.getMessage())
                            .codigoError("1012")
                            .httpStatusCode(500)
                            .build(), e
            );
        }
    }

    public ApiResponse refreshToken () {
        String userId = jwt.getSubject();
        User user = userRepository.findById(Long.valueOf(userId));

        String token =  jwtTokenFactory.generateToken(user.getUsername(), Long.valueOf(userId), user.getRole());

        Map<String, String> map = new HashMap<>();

        map.put("accessToken", token);

        return ApiResponse.success(map,"User Authenticated Successfully");
    }
}