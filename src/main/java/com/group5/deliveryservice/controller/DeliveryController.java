package com.group5.deliveryservice.controller;

import com.group5.deliveryservice.dto.*;
import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    private final static int ONE_STEP_STATUS_CHANGE = -1;

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public List<Delivery> getAll() {
        return deliveryService.getAll();
    }

    @DeleteMapping("/{deliveryId}")
    public void deleteDelivery(@PathVariable String deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable String deliveryId) {
        var delivery = deliveryService.findDeliveryById(deliveryId);
        return ResponseEntity.ok().body(delivery);
    }

    @GetMapping("/customer/{customerId}/status/active")
    public ResponseEntity<List<Delivery>> getActiveDeliveriesOfCustomer(@PathVariable String customerId) {

        List<Delivery> delivery = deliveryService.getActiveDeliveriesOfCustomer(customerId);
        return ResponseEntity.ok().body(delivery);
    }

    @GetMapping("/customer/{customerId}/status/delivered")
    public ResponseEntity<List<Delivery>> getPastDeliveriesOfCustomer(@PathVariable String customerId) {
        List<Delivery> delivery = deliveryService.getPastDeliveriesOfCustomer(customerId);
        return ResponseEntity.ok().body(delivery);
    }

    @PostMapping
    public Delivery createDelivery(@RequestBody final CreateDeliveryDto createDeliveryDto) {
        return deliveryService.createDelivery(createDeliveryDto);
    }

    @PutMapping("/deliveryStatus")
    public ResponseEntity<List<Delivery>> updateDeliveryStatus(DeliveryStatusUpdate deliveryStatusUpdate) {
        List<Delivery> updatedEntity = null;
        if (deliveryStatusUpdate instanceof DeliveryCollectedDto) {
            updatedEntity = deliveryService.changeStatusToCollected((DeliveryCollectedDto) deliveryStatusUpdate);
        } else if (deliveryStatusUpdate instanceof DeliveryDepositedDto) {
            updatedEntity = deliveryService.changeStatusToDeposited((DeliveryDepositedDto) deliveryStatusUpdate);
        } else if (deliveryStatusUpdate instanceof DeliveryDeliveredDto) {
            updatedEntity = deliveryService.changeStatusToDelivered((DeliveryDeliveredDto) deliveryStatusUpdate);
        }

        return ResponseEntity.ok().body(updatedEntity);
    }
}


