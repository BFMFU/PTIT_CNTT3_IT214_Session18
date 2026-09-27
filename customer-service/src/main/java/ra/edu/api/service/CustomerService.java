package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.dto.CustomerRequest;
import ra.edu.api.entity.Customer;
import ra.edu.api.exception.DuplicateResourceException;
import ra.edu.api.exception.ResourceNotFoundException;
import ra.edu.api.repository.CustomerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Cacheable(value = "customers", key = "#id")
    public Customer findById(Long id) {
        log.info("Cache MISS - loading customer {} from DB", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional
    public Customer create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        if (customerRepository.existsByIdCard(request.getIdCard())) {
            throw new DuplicateResourceException("ID card already exists: " + request.getIdCard());
        }
        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .idCard(request.getIdCard())
                .dateOfBirth(request.getDateOfBirth())
                .isActive(true)
                .build();
        Customer saved = customerRepository.save(customer);
        log.info("Customer created: {}", saved.getId());
        return saved;
    }

    @Transactional
    @CachePut(value = "customers", key = "#id")
    public Customer update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        return customerRepository.save(customer);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public void delete(Long id) {
        Customer customer = findById(id);
        customer.setActive(false);  // Soft delete
        customerRepository.save(customer);
        log.info("Customer soft-deleted: {}", id);
    }
}
