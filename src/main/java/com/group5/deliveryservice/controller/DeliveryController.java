package com.group5.deliveryservice.controller;

import com.group5.deliveryservice.mail.MailService;
import com.group5.deliveryservice.mail.StatusChangeMailRequest;
import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.model.Role;
import com.group5.deliveryservice.model.User;
import com.group5.deliveryservice.repository.BoxRepository;
import com.group5.deliveryservice.repository.DeliveryRepository;
import com.group5.deliveryservice.repository.UserRepository;
import com.group5.deliveryservice.service.CodeGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class DeliveryController {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoxRepository boxRepository;

    @Autowired
    private CodeGeneratorService trackingCodeGeneratorService;

    @Autowired
    private MailService mailService;

    private final static int ONE_STEP_STATUS_CHANGE = -1;

    private void checkValidDeliveryStatus(Delivery delivery, DeliveryStatus deliveryStatus) {
        if (delivery.getDeliveryStatus().compareTo(deliveryStatus) != ONE_STEP_STATUS_CHANGE || !delivery.isActive())
            throw new RuntimeException("Delivery with id " + delivery.getId() + " can not be updated since new delivery status is not valid");
    }

    public Map<String, Boolean> deleteBox(Delivery delivery) {
        deliveryRepository.delete(delivery);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @GetMapping("/deliveries/all")
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    @GetMapping("/deliveries/{id}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable(value = "id") long deliveryId)
            throws RuntimeException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for id " + deliveryId));
        return ResponseEntity.ok().body(delivery);
    }

    @GetMapping("/deliveries/active")
    public ResponseEntity<List<Delivery>> getActiveDeliveriesByCustomerId(@RequestParam long customerId)
            throws RuntimeException {
        List<Delivery> delivery = deliveryRepository.findAllByCustomerIdAndActiveTrue(customerId);
        return ResponseEntity.ok().body(delivery);
    }

    @GetMapping("/deliveries/past")
    public ResponseEntity<List<Delivery>> getPastDeliveriesByCustomerId(@RequestParam long customerId)
            throws RuntimeException {
        List<Delivery> delivery = deliveryRepository.findAllByCustomerIdAndActiveFalse(customerId);
        return ResponseEntity.ok().body(delivery);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<Delivery> getDeliveryByTrackingId(@RequestParam String trackingId)
            throws RuntimeException {
        Delivery delivery = deliveryRepository.findByTrackingIdAndActiveTrue(trackingId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for tracking id " + trackingId));
        return ResponseEntity.ok().body(delivery);
    }

    @PostMapping("/deliveries")
    public Delivery createDeliveryAndAssign(@RequestParam long boxId,
                                            @RequestParam long customerId,
                                            @RequestParam long delivererId,
                                            @RequestParam String description) throws RuntimeException {
        Delivery delivery = new Delivery(description, DeliveryStatus.CREATED, true);

        if (!deliveryRepository.findAllByCustomerIdNotAndBoxIdAndActiveTrue(customerId, boxId).isEmpty())
            throw new RuntimeException("Box with id " + boxId + " is already in use by another customer");
        if (boxRepository.findById(boxId).isPresent())
            delivery.setBoxId(boxId);
        else
            throw new RuntimeException("Box with id " + boxId + " does not exist");

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer with id " + customerId + " does not exist"));
        delivery.setCustomerId(customerId);

        Optional<User> deliverer = userRepository.findById(delivererId);
        if (deliverer.isPresent() && deliverer.get().getRole() == Role.DELIVERER)
            delivery.setDelivererId(delivererId);
        else
            throw new RuntimeException("Deliverer with id " + delivererId + " does not exist");

        delivery.setAssigned_at(new Date());
        var mailRequest = new StatusChangeMailRequest(DeliveryStatus.CREATED, delivery.getTrackingId());
        mailService.sendEmailTo(customer.getEmail(), mailRequest);

        return deliveryRepository.save(delivery);
    }

    @PutMapping("/deliveries/collected/{ids}")
    public ResponseEntity<List<Delivery>> updateDeliveryStatusCollected(@PathVariable(value = "ids") long[] deliveryIds) throws RuntimeException {
        List<Delivery> updatedDeliveries = new LinkedList<>();
        Delivery delivery;
        for (long deliveryId : deliveryIds) {
            delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found for id " + deliveryId));
            checkValidDeliveryStatus(delivery, DeliveryStatus.COLLECTED);
            delivery.setCollected_at(new Date());
            delivery.setDeliveryStatus(DeliveryStatus.COLLECTED);
            updatedDeliveries.add(delivery);
        }
        return ResponseEntity.ok(deliveryRepository.saveAll(updatedDeliveries));
    }

    @PutMapping("/deliveries/deposited")
    public ResponseEntity<List<Delivery>> updateDeliveryStatusDeposited(@RequestParam long delivererId, @RequestParam long boxId) throws RuntimeException {
        User deliverer = userRepository.findById(delivererId)
                .orElseThrow(() -> new RuntimeException("Deliverer not found for id " + delivererId));
        List<Delivery> deliveries = deliveryRepository.findAllByDeliveryStatusAndDelivererIdAndBoxIdAndActiveTrue(DeliveryStatus.COLLECTED, deliverer.getId(), boxId);
        if (deliveries.isEmpty())
            throw new RuntimeException("No delivery found for deliverer id " + deliverer.getId() + " and box id " + boxId);
        for (Delivery delivery : deliveries) {
            checkValidDeliveryStatus(delivery, DeliveryStatus.DEPOSITED);
            delivery.setDeposited_at(new Date());
            delivery.setDeliveryStatus(DeliveryStatus.DEPOSITED);
        }

        User customer = userRepository.findById(deliveries.get(0).getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer with id " + deliveries.get(0).getCustomerId() + " does not exist"));

        var mailRequest = new StatusChangeMailRequest(DeliveryStatus.DEPOSITED);
        mailService.sendEmailTo(customer.getEmail(), mailRequest);

        return ResponseEntity.ok(deliveryRepository.saveAll(deliveries));
    }

    @PutMapping("/deliveries/delivered")
    public ResponseEntity<List<Delivery>> updateDeliveryStatusDelivered(@RequestParam long customerId, @RequestParam long boxId) throws RuntimeException {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found for id " + customerId));
        List<Delivery> deliveries = deliveryRepository.findAllByDeliveryStatusAndCustomerIdAndBoxIdAndActiveTrue(DeliveryStatus.DEPOSITED, customerId, boxId);
        if (deliveries.isEmpty())
            throw new RuntimeException("No delivery found for customer id " + customerId + " and box id " + boxId);
        for (Delivery delivery : deliveries) {
            checkValidDeliveryStatus(delivery, DeliveryStatus.DELIVERED);
            delivery.setDeliveryStatus(DeliveryStatus.DELIVERED);
            delivery.setActive(false);
            delivery.setDelivered_at(new Date());
        }

        var mailRequest = new StatusChangeMailRequest(DeliveryStatus.DELIVERED);
        mailService.sendEmailTo(customer.getEmail(), mailRequest);

        return ResponseEntity.ok(deliveryRepository.saveAll(deliveries));
    }

    @DeleteMapping("/deliveries/{id}")
    public Map<String, Boolean> deleteDelivery(@PathVariable(value = "id") Long deliveryId)
            throws RuntimeException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + deliveryId));
        return deleteBox(delivery);
    }

}


