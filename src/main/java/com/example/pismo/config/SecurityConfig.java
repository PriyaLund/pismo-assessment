package com.example.pismo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // configurable via application.yml or env (API_PASSWORD)
    @Value("${app.security.user:api}")
    private String username;

    @Value("${app.security.password:pismo123}")
    private String rawPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // stateless API
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // allow H2 console frames
                .authorizeHttpRequests(auth -> auth
                        // allow docs & H2 without auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()
                        // everything else needs auth
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Basic auth

        return http.build();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        var user = User.withUsername(username)
                .password(encoder.encode(rawPassword))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

