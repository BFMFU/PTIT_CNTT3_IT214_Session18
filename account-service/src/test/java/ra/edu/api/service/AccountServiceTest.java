package ra.edu.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ra.edu.api.dto.AccountRequest;
import ra.edu.api.entity.Account;
import ra.edu.api.exception.ResourceNotFoundException;
import ra.edu.api.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("VN202409270001")
                .customerId(100L)
                .accountTypeId(1L)
                .balance(new BigDecimal("5000000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("findById should return account when found")
    void findById_ShouldReturnAccount_WhenFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Account result = accountService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("VN202409270001", result.getAccountNumber());
        assertEquals(new BigDecimal("5000000.00"), result.getBalance());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when not found")
    void findById_ShouldThrow_WhenNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.findById(99L));
        verify(accountRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("create should save account with generated account number")
    void create_ShouldSaveAccount_WithGeneratedNumber() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(100L);
        request.setAccountTypeId(1L);
        request.setInitialBalance(new BigDecimal("1000000.00"));

        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.create(request);

        assertNotNull(result);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("close should set account status to CLOSED")
    void close_ShouldSetStatusToClosed() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.close(1L);

        verify(accountRepository, times(1)).save(argThat(a ->
                a.getStatus() == Account.AccountStatus.CLOSED));
    }
}
