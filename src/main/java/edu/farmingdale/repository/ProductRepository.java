package edu.farmingdale.repository;

import edu.farmingdale.model.Product;

import java.util.List;

public interface ProductRepository extends Repository<Product, Integer> {
    List<Product> findLowStockProducts();

    List<Product> findBySupplierId(Integer supplierId);

    List<Product> search(String query);
}
