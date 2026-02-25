package antifraud.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRequest {
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @NotBlank(message = "Password cannot be empty")
    private String password;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
