package com.eduhub.auth_service.security;

import com.eduhub.auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor

public class JwtAuthenticationFilter implements WebFilter {

    private  JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange.getRequest().getHeaders());

        if (token == null) {
            return chain.filter(exchange);
        }

        return jwtService.validateToken(token)
                .flatMap(valid -> {
                    if (!valid) {
                        return chain.filter(exchange);
                    }

                    return jwtService.getUsername(token)
                            .zipWith(jwtService.getAuthorities(token))
                            .flatMap(tuple -> {
                                String username = tuple.getT1();
                                List<GrantedAuthority> authorities = tuple.getT2();  // Explicit List<GrantedAuthority>

                                if (username == null || authorities == null) {
                                    return chain.filter(exchange);
                                }

                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        authorities  // This now matches the expected type
                                );

                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                            });
                })
                .onErrorResume(e -> chain.filter(exchange));
    }

    private String extractToken(HttpHeaders headers) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}