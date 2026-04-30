package edu.farmingdale.repository;

import edu.farmingdale.model.SalesOrder;
import edu.farmingdale.model.enums.SalesOrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface SalesOrderRepository extends Repository<SalesOrder, Integer> {
    List<SalesOrder> findByCustomerId(Integer customerId);

    List<SalesOrder> findByStatus(SalesOrderStatus status);

    List<SalesOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
}
