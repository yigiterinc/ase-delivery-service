package com.group5.deliveryservice.repository;

import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, String> {

    List<Delivery> getDeliveriesByCustomerId(String customerId);

    void deleteById(String deliveryId);

    List<Delivery> findAllByDeliveryStatusAndDelivererId(DeliveryStatus created, String delivererId);

    List<Delivery> findAllByTargetPickupBoxIdAndDeliveryStatus(String boxId, DeliveryStatus deliveryStatus);
}
