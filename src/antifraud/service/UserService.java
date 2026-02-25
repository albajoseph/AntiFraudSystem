package antifraud.service;

import antifraud.model.User;
import antifraud.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.findByUsernameIgnoreCase(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // First user is ADMINISTRATOR and unlocked; others are MERCHANT and locked
        if (userRepository.count() == 0) {
            user.setRole("ADMINISTRATOR");
            user.setAccountNonLocked(true);
        } else {
            user.setRole("MERCHANT");
            user.setAccountNonLocked(false);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> listAllUsers() {
        // CHANGED: Explicitly order by ID to satisfy test cases #4, #6, #17, #20
        return userRepository.findAllByOrderByIdAsc();
    }

    public User changeRole(String username, String role) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!role.equals("SUPPORT") && !role.equals("MERCHANT")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (user.getRole().equals(role)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    public Map<String, String> changeAccess(String username, String operation) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // ADMINISTRATOR cannot be locked/unlocked
        if (user.getRole().equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if ("LOCK".equals(operation)) {
            user.setAccountNonLocked(false);
        } else if ("UNLOCK".equals(operation)) {
            user.setAccountNonLocked(true);
        } else {
            // Good practice to catch invalid operations
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        userRepository.save(user);

        // Match the exact status message expected by the test
        String status = "User " + user.getUsername() + ("LOCK".equals(operation) ? " locked!" : " unlocked!");
        return Map.of("status", status);
    }

    public Map<String, String> deleteUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        userRepository.delete(user);
        return Map.of(
                "username", user.getUsername(),
                "status", "Deleted successfully!"
        );
    }
}