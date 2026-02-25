package antifraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private String role; // MERCHANT, ADMINISTRATOR, or SUPPORT

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean isAccountNonLocked; // Logic for LOCK/UNLOCK

    // Constructors
    public User() {}

    public User(String name, String username, String password, String role, boolean isAccountNonLocked) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isAccountNonLocked = isAccountNonLocked;
    }

    // Spring Security UserDetails Methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Roles in Spring Security usually require the "ROLE_" prefix
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isAccountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public void setAccountNonLocked(boolean accountNonLocked) { isAccountNonLocked = accountNonLocked; }
}