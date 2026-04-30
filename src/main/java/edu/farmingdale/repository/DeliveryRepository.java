package edu.farmingdale.repository;

import edu.farmingdale.model.Delivery;
import edu.farmingdale.model.enums.DeliveryStatus;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryRepository extends Repository<Delivery, Integer> {
    List<Delivery> findByPurchaseOrderId(Integer purchaseOrderId);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findUpcomingDeliveries(LocalDate startDate, LocalDate endDate);
}
