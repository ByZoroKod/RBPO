package org.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errorHandler,
                                                   JwtService jwtService, UserDetailsService userDetailsService)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService, errorHandler),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/refresh")
                                .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/bids", "/api/auction-lots/*/place-bid")
                                .hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/users")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users", "/api/auction-lots", "/api/auction-deals")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/auction-lots/*", "/api/bids/*", "/api/auction-deals/*")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*", "/api/auction-lots/*", "/api/bids/*",
                                "/api/auction-deals/*").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder,
                                                       AuthenticationEventPublisher eventPublisher) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        ProviderManager manager = new ProviderManager(provider);
        manager.setAuthenticationEventPublisher(eventPublisher);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
