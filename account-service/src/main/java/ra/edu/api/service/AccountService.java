package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.dto.AccountRequest;
import ra.edu.api.entity.Account;
import ra.edu.api.exception.ResourceNotFoundException;
import ra.edu.api.repository.AccountRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Cacheable(value = "accounts", key = "#id")
    public Account findById(Long id) {
        log.info("Cache MISS - loading account {} from DB", id);
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    public List<Account> findByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Account create(AccountRequest request) {
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .customerId(request.getCustomerId())
                .accountTypeId(request.getAccountTypeId())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance()
                        : java.math.BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .createdByStaffId(request.getCreatedByStaffId())
                .build();
        Account saved = accountRepository.save(account);
        log.info("Account created: {} for customer {}", saved.getAccountNumber(), saved.getCustomerId());
        return saved;
    }

    @Transactional
    @CachePut(value = "accounts", key = "#id")
    public Account update(Long id, AccountRequest request) {
        Account account = findById(id);
        account.setAccountTypeId(request.getAccountTypeId());
        account.setStatus(Account.AccountStatus.valueOf(
                request.getStatus() != null ? request.getStatus() : "ACTIVE"));
        return accountRepository.save(account);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#id")
    public void close(Long id) {
        Account account = findById(id);
        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);
        log.info("Account {} closed", id);
    }

    private String generateAccountNumber() {
        // Format: VN + YYYYMMDD + 6 random digits
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "VN" + date + random;
    }
}
