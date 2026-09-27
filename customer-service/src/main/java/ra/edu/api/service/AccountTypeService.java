package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.dto.AccountTypeRequest;
import ra.edu.api.entity.AccountType;
import ra.edu.api.exception.DuplicateResourceException;
import ra.edu.api.exception.ResourceNotFoundException;
import ra.edu.api.repository.AccountTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;

    @Cacheable(value = "account-types", key = "'all'")
    public List<AccountType> findAll() {
        log.info("Cache MISS - loading all account types from DB");
        return accountTypeRepository.findAll();
    }

    @Cacheable(value = "account-types", key = "#id")
    public AccountType findById(Long id) {
        return accountTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountType not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "account-types", allEntries = true)
    public AccountType create(AccountTypeRequest request) {
        if (accountTypeRepository.existsByTypeName(request.getTypeName())) {
            throw new DuplicateResourceException("Account type already exists: " + request.getTypeName());
        }
        AccountType at = AccountType.builder()
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .interestRate(request.getInterestRate())
                .minBalance(request.getMinBalance())
                .build();
        return accountTypeRepository.save(at);
    }

    @Transactional
    @CacheEvict(value = "account-types", allEntries = true)
    public AccountType update(Long id, AccountTypeRequest request) {
        AccountType at = findById(id);
        at.setTypeName(request.getTypeName());
        at.setDescription(request.getDescription());
        at.setInterestRate(request.getInterestRate());
        at.setMinBalance(request.getMinBalance());
        return accountTypeRepository.save(at);
    }

    @Transactional
    @CacheEvict(value = "account-types", allEntries = true)
    public void delete(Long id) {
        AccountType at = findById(id);
        accountTypeRepository.delete(at);
        log.info("AccountType deleted: {}", id);
    }
}
