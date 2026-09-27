package ra.edu.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.api.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
