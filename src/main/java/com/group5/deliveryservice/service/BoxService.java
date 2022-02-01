package com.group5.deliveryservice.service;

import com.group5.deliveryservice.exception.BoxNotFoundException;
import com.group5.deliveryservice.model.Box;
import com.group5.deliveryservice.repository.BoxRepository;
import org.springframework.stereotype.Service;

@Service
public class BoxService {

    private final BoxRepository boxRepository;

    public BoxService(BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    public Box findById(String id) {
        return boxRepository.findById(id)
                .orElseThrow(
                        () -> new BoxNotFoundException(
                                String.format("Box with id %s not found while creating delivery", id)));
    }

    public boolean isBoxAvailableForNewDelivery(String customerId, String boxId) {
        var box = findById(boxId);
        return boxOnlyContainsDeliveriesOfThisCustomer(box, customerId);
    }

    private boolean boxOnlyContainsDeliveriesOfThisCustomer(Box box, String customerId) {
        var boxIsEmpty = box.getContainedDeliveries().isEmpty();
        var containsDeliveriesOfThisCustomerOnly = box.getContainedDeliveries()
                .stream().allMatch(delivery -> delivery.getCustomerId().equals(customerId));

        return boxIsEmpty || containsDeliveriesOfThisCustomerOnly;
    }
}
