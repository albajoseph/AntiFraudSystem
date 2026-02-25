package antifraud.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                // Disable CSRF for REST API testing
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                // Modernized frameOptions to support H2 console/frames
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(requests -> requests
                        // Public Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // Role-Based Access Control
                        // Removed the trailing /** from /list to match exact endpoint requirements
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")

                        // Stage 1-3: Merchant transaction
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")

                        // Stage 4-6: SUPPORT Role
                        .requestMatchers("/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
                        .requestMatchers("/api/antifraud/stolencard/**").hasRole("SUPPORT")
                        .requestMatchers("/api/antifraud/history/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT")

                        // Administrator Endpoints
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMINISTRATOR")

                        // Catch-all
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}