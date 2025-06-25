package com.eduhub.auth_service.config;

import com.eduhub.auth_service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(PasswordEncoder passwordEncoder) {
        return username -> Mono.just(
                User.withUsername(username)
                        .password(passwordEncoder.encode("dummy")) // not used; JWT handles auth
                        .roles("USER")
                        .build()
        );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // uses WebConfig's corsConfigurationSource bean
                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/reset-password-request",
                                "/auth/reset-password",
                                "/auth/mfa",
                                "/auth/refresh"
                        ).permitAll()
                        .pathMatchers("/auth/**").hasRole("ADMIN")
                        .pathMatchers("/login/oauth2/code/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
