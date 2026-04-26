package server.core.repository;

import common.entity.Customer;

import java.util.List;

public interface CustomerRepository {
    List<Customer> findAll();

    List<Customer> findByMinLoyalty(int loyaltyPoints);

    List<Customer> findByNameContaining(String keyword);

    Customer findById(String customerId);

    Customer findByPhone(String phone);

    Customer findByEmail(String email);

    Customer findByIdCard(String idCard);

    boolean add(Customer customer);

    boolean update(Customer customer);
}