package edu.farmingdale.repository;

import edu.farmingdale.model.PurchaseOrder;
import edu.farmingdale.model.enums.PurchaseOrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderRepository extends Repository<PurchaseOrder, Integer> {
    List<PurchaseOrder> findBySupplierId(Integer supplierId);

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    List<PurchaseOrder> findByExpectedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);
}
