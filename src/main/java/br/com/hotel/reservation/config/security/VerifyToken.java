package br.com.hotel.reservation.config.security;

import br.com.hotel.reservation.repository.UserRepository;
import br.com.hotel.reservation.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class VerifyToken extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final var authHeader = request.getHeader("Authorization");
        if (!Objects.isNull(authHeader)) {
            final var token = tokenService.extractToken(authHeader);
            final var email = tokenService.verifyToken(token);
            if (!email.isBlank()) {
                final var user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                final var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
