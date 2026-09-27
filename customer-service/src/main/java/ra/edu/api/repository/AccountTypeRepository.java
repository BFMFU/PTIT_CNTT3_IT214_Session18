package ra.edu.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.api.entity.AccountType;

public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {
    boolean existsByTypeName(String typeName);
}
