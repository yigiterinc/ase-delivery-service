package com.group5.deliveryservice.repository;

import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, Long> {

    List<Delivery> findAllByDeliveryStatusAndDelivererIdAndBoxIdAndActiveTrue(DeliveryStatus deliveryStatus, long delivererId, long boxId);

    List<Delivery> findAllByDeliveryStatusAndCustomerIdAndBoxIdAndActiveTrue(DeliveryStatus deliveryStatus, long customerId, long boxId);

    List<Delivery> findAllByDeliveryStatusAndDelivererId(DeliveryStatus deliveryStatus, long delivererId);

    List<Delivery> findAllByCustomerIdAndActiveTrue(long customerId);

    List<Delivery> findAllByCustomerIdAndActiveFalse(long customerId);

    Optional<Delivery> findByTrackingIdAndActiveTrue(String trackingId);

    List<Delivery> findAllByCustomerIdNotAndBoxIdAndActiveTrue(long customerId, long boxId);

}
