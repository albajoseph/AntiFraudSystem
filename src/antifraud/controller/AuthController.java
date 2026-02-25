package antifraud.controller;

import antifraud.dto.UserRequest;
import antifraud.dto.UserResponse;
import antifraud.model.User;
import antifraud.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists!");
        }

        // The first registered user receives the ADMINISTRATOR role; the rest — MERCHANT
        String role = userRepository.count() == 0 ? "ADMINISTRATOR" : "MERCHANT";

        // All users, except ADMINISTRATOR, must be locked immediately after registration
        boolean isUnlocked = role.equals("ADMINISTRATOR");

        User user = new User(
                request.getName(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role,
                isUnlocked
        );
        userRepository.save(user);

        // Response should include the role field
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponse>> listUsers() {
        // Cleaned up stream: maps the User entity directly to the 4-argument UserResponse constructor
        List<UserResponse> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getUsername(), u.getRole()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);

        return ResponseEntity.ok(Map.of(
                "username", username,
                "status", "Deleted successfully!"
        ));
    }

    @PutMapping("/role")
    public ResponseEntity<UserResponse> changeRole(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String role = request.get("role");

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // If a role is not SUPPORT or MERCHANT, respond with HTTP Bad Request status (400)
        if (!role.equals("SUPPORT") && !role.equals("MERCHANT")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // If you want to assign a role that has been already provided to a user, respond with 409 Conflict
        if (user.getRole().equals(role)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole()));
    }

    @PutMapping("/access")
    public ResponseEntity<Map<String, String>> changeAccess(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String operation = request.get("operation"); // LOCK or UNLOCK

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // For safety reasons, ADMINISTRATOR cannot be blocked. Respond with 400 Bad Request
        if (user.getRole().equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean lock = operation.equals("LOCK");
        user.setAccountNonLocked(!lock);
        userRepository.save(user);

        // Response format: "User <username> <[locked, unlocked]>!"
        String status = String.format("User %s %s!", username, lock ? "locked" : "unlocked");
        return ResponseEntity.ok(Map.of("status", status));
    }
}