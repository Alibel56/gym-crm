package com.practice.controller;

import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.TraineeRegistrationRequest;
import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.dto.request.UpdateLoginRequest;
import com.practice.dto.response.RegistrationResponse;
import com.practice.dto.response.LoginResponse;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Authentication",
        description = "Registration, authentication and password management"
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private UserService userService;
    private TraineeService traineeService;
    private TrainerService trainerService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Operation(
            summary = "Register trainee",
            description = "Creates a new trainee account and returns generated username and password."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trainee registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is already registered"
            )
    })
    @PostMapping("/trainee/register")
    public ResponseEntity<RegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        RegistrationResponse response = traineeService.create(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register trainer",
            description = "Creates a new trainer account and returns generated username and password."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trainer registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data or training type"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is already registered"
            )
    })
    @PostMapping("/trainer/register")
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        RegistrationResponse response = trainerService.create(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Login",
            description = "Validates that the given username and password match."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credentials are valid"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(userService.login(request));
    }

    @Operation(
            summary = "Change password",
            description = "Changes the password of an existing user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or old password"
            )
    })
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UpdateLoginRequest request
    ) {
        userService.changePassword(request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Logout", description = "Revokes the current JWT token.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        userService.logout(authorizationHeader);
        return ResponseEntity.noContent().build();
    }
}