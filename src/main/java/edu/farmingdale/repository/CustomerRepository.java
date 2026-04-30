package edu.farmingdale.repository;

import edu.farmingdale.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends Repository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);

    List<Customer> search(String query);
}
