package org.example.security;

import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttempts loginAttempts;

    public AppUserDetailsService(UserRepository userRepository, LoginAttempts loginAttempts) {
        this.userRepository = userRepository;
        this.loginAttempts = loginAttempts;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(user -> new AppUserDetails(user, loginAttempts.isLocked(username)))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
