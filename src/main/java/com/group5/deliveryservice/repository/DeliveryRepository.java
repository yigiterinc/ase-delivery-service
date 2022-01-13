package com.group5.deliveryservice.repository;

import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, Long> {
    Optional<Delivery> findByDelivererIdAndBoxIdAndIsActiveTrue(long delivererId, long boxId);

    Optional<Delivery> findByCustomerIdAndBoxIdAndIsActiveTrue(long customerId, long boxId);

    Optional<Delivery> findByTrackingId(String trackingCode);

    List<Delivery> findAllByCustomerIdNotAndBoxIdAndIsActiveTrue(long customerId, long boxId);

    List<Delivery> findAllByBoxIdAndCustomerIdAndDeliveryStatusAndIsActiveTrue(long boxId, long userId, DeliveryStatus deliveryStatus);

}
