package com.clinic.appointmentsystem.infrastructure.security;

import com.clinic.appointmentsystem.persistence.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        String auth = ((HttpServletRequest) req).getHeader(HttpHeaders.AUTHORIZATION);
        String requestURI = ((HttpServletRequest) req).getRequestURI();
        log.debug("Processing request to {} with auth header: {}", requestURI, auth != null ? "present" : "missing");

        if (auth == null || !auth.startsWith("Bearer ")) {
            log.debug("No valid Bearer token found for request to {}", requestURI);
            chain.doFilter(req, res);
            return;
        }

        try {
            String token = auth.substring(7);
            Claims claims = jwtService.parse(token).getBody();

            UUID userId = UUID.fromString(claims.getSubject());
            var user = userRepo.findById(userId).orElse(null);
            if (user != null) {
                log.debug("Authenticated user {} for request to {}", user.getEmail(), requestURI);
                var userDetails = new CustomUserDetails(
                    user.getId(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                    true
                );
                var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("User not found for ID {} in request to {}", userId, requestURI);
            }
        } catch (Exception e) {
            log.error("Error processing JWT token for request to {}: {}", requestURI, e.getMessage());
        }
        chain.doFilter(req, res);
    }
}
