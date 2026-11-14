package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService,
                                   AuthenticationEntryPoint entryPoint) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.entryPoint = entryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String username = jwtService.validateAccessToken(header.substring(BEARER.length())).getSubject();
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (!user.isEnabled()) {
                throw new DisabledException("Пользователь заблокирован");
            }
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, e);
            return;
        }
        chain.doFilter(request, response);
    }
}
