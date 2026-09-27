package ra.edu.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ra.edu.api.entity.User;
import ra.edu.api.repository.UserRepository;

/**
 * Initializes default ADMIN user if none exists.
 * Default credentials: admin / Admin@123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Default ADMIN user created: admin / Admin@123");
        }
    }
}
