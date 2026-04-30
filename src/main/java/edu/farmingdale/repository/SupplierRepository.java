package edu.farmingdale.repository;

import edu.farmingdale.model.Supplier;

import java.util.List;

public interface SupplierRepository extends Repository<Supplier, Integer> {
    List<Supplier> search(String query);
}
