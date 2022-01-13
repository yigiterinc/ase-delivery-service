package com.group5.deliveryservice.controller;

import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.model.Role;
import com.group5.deliveryservice.model.User;
import com.group5.deliveryservice.repository.BoxRepository;
import com.group5.deliveryservice.repository.DeliveryRepository;
import com.group5.deliveryservice.repository.UserRepository;
import com.group5.deliveryservice.service.EmailNotificationService;
import com.group5.deliveryservice.service.SequenceGeneratorService;
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
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private CodeGeneratorService trackingCodeGeneratorService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    private final static int ONE_STEP_STATUS_CHANGE = -1;


    private ResponseEntity<List<Delivery>> changeDelivery(Delivery delivery, DeliveryStatus deliveryStatus) {
        delivery.setDeliveryStatus(deliveryStatus);
        return ResponseEntity.ok(Arrays.asList(deliveryRepository.save(delivery)));
    }

    private ResponseEntity<List<Delivery>> changeRelatedDeliveries(Delivery delivery, Date timestamp) {
        List<Delivery> relatedDeliveries = deliveryRepository.findAllByBoxIdAndCustomerIdAndDeliveryStatusAndIsActiveTrue(delivery.getBoxId(), delivery.getCustomerId(), DeliveryStatus.DEPOSITED);
        for (Delivery relatedDelivery : relatedDeliveries) {
            relatedDelivery.setDeliveryStatus(DeliveryStatus.ASSIGNED);
            relatedDelivery.setActive(false);
            relatedDelivery.setDelivered_at(timestamp);
        }
        return ResponseEntity.ok(deliveryRepository.saveAll(relatedDeliveries));

    }

    private ResponseEntity<List<Delivery>> updateDeliveryStatus(Delivery delivery, DeliveryStatus deliveryStatus) {
        if (delivery.getDeliveryStatus().compareTo(deliveryStatus) != ONE_STEP_STATUS_CHANGE || !delivery.isActive())
            throw new RuntimeException("Delivery with id " + delivery.getId() + " can not be updated since new delivery status is not valid");

        ResponseEntity<List<Delivery>> response;
        Date currentTimestamp = new Date();
        if (deliveryStatus != DeliveryStatus.DELIVERED) {
            switch (deliveryStatus) {
                case ASSIGNED:
                    delivery.setAssigned_at(currentTimestamp);
                    break;
                case COLLECTED:
                    delivery.setCollected_at(currentTimestamp);
                    break;
                case DEPOSITED:
                    delivery.setDeposited_at(currentTimestamp);
                    User user = userRepository.findById(delivery.getCustomerId())
                            .orElseThrow(() -> new RuntimeException("Customer with id " + delivery.getCustomerId() + " does not exist"));
                    emailNotificationService.sendDepositedDeliveryEmailNotification(user.getEmail(), user.getLastName());
                    break;
            }
            response = changeDelivery(delivery, deliveryStatus);
        } else {
            response = changeRelatedDeliveries(delivery, currentTimestamp);
            User user = userRepository.findById(delivery.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer with id " + delivery.getCustomerId() + " does not exist"));
            emailNotificationService.sendDeliveredDeliveryEmailNotification(user.getEmail(), user.getLastName());
        }

        return response;
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

    @GetMapping("/deliveries")
    public ResponseEntity<Delivery> getDeliveryByTrackingId(@RequestParam(value = "trackingId") String trackingId)
            throws RuntimeException {
        Delivery delivery = deliveryRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for tracking id " + trackingId));
        return ResponseEntity.ok().body(delivery);
    }

    @PostMapping("/deliveries")
    public Delivery createDeliveryAndAssign(@RequestParam long boxId,
                                            @RequestParam long customerId,
                                            @RequestParam long delivererId,
                                            @RequestParam String description) throws RuntimeException {
        Delivery delivery = new Delivery(sequenceGeneratorService.generateSequence(Delivery.SEQUENCE_NAME),
                trackingCodeGeneratorService.generateSequence(Delivery.TRACKING_CODE_SEQUENCE_NAME), description, DeliveryStatus.ASSIGNED, true);

        if (!deliveryRepository.findAllByCustomerIdNotAndBoxIdAndIsActiveTrue(customerId, boxId).isEmpty())
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
        emailNotificationService.sendCreatedDeliveryNotification(customer.getEmail(), customer.getLastName(), delivery.getTrackingId());
        return deliveryRepository.save(delivery);
    }

    @PutMapping("/deliveries/status/collected/{id}")
    public ResponseEntity<List<Delivery>> updateDeliveryStatusCollected(@RequestParam long deliveryId) throws RuntimeException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for id " + deliveryId));
        return updateDeliveryStatus(delivery, DeliveryStatus.COLLECTED);
    }

    @PutMapping("/deliveries/status/deposited")
    public ResponseEntity<List<Delivery>> updateDeliveryStatusDeposited(@RequestParam long delivererId, @RequestParam long boxId) throws RuntimeException {
        User deliverer = userRepository.findById(delivererId)
                .orElseThrow(() -> new RuntimeException("Deliverer not found for id " + delivererId));
        Delivery delivery = deliveryRepository.findByDelivererIdAndBoxIdAndIsActiveTrue(deliverer.getId(), boxId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for deliverer id " + deliverer.getId() + " and box id " + boxId));
        return updateDeliveryStatus(delivery, DeliveryStatus.DEPOSITED);
    }

    @PutMapping("/deliveries/status/delivered")
    public ResponseEntity<List<Delivery>> updateDeliveryStatusDelivered(@RequestParam long customerId, @RequestParam long boxId) throws RuntimeException {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found for rfid token " + customerId));
        Delivery delivery = deliveryRepository.findByCustomerIdAndBoxIdAndIsActiveTrue(customer.getId(), boxId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for customer id " + customer.getId() + " and box id " + boxId));
        return updateDeliveryStatus(delivery, DeliveryStatus.DELIVERED);
    }

    @DeleteMapping("/deliveries/{id}")
    public Map<String, Boolean> deleteDelivery(@PathVariable(value = "id") Long deliveryId)
            throws RuntimeException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + deliveryId));
        return deleteBox(delivery);
    }

    @DeleteMapping("/deliveries")
    public Map<String, Boolean> deleteDelivery(@RequestParam(value = "trackingId") String trackingId)
            throws RuntimeException {
        Delivery delivery = deliveryRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Box not found for name " + trackingId));
        return deleteBox(delivery);
    }
}


