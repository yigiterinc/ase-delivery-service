package com.group5.deliveryservice.service;

import com.group5.deliveryservice.dto.CreateDeliveryDto;
import com.group5.deliveryservice.dto.DeliveryCollectedDto;
import com.group5.deliveryservice.dto.DeliveryDeliveredDto;
import com.group5.deliveryservice.dto.DeliveryDepositedDto;
import com.group5.deliveryservice.mail.MailService;
import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.repository.BoxRepository;
import com.group5.deliveryservice.repository.DeliveryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final BoxRepository boxRepository;
    private final MailService mailService;

    public DeliveryService(final DeliveryRepository deliveryRepository,
                           final BoxRepository boxRepository,
                           final MailService mailService) {
        this.deliveryRepository = deliveryRepository;
        this.boxRepository = boxRepository;
        this.mailService = mailService;
    }

    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }

    public Delivery findDeliveryById(final String deliveryId) {
        return deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new RuntimeException("Delivery not found for id " + deliveryId));
    }

    public void deleteDelivery(String deliveryId) {
        deliveryRepository.deleteById(deliveryId);
    }

    public List<Delivery> getActiveDeliveriesOfCustomer(final String customerId) {
        return this.deliveryRepository.getDeliveriesByCustomerId(customerId).stream()
                .filter(delivery -> isActiveDelivery.test(delivery))
                .collect(Collectors.toList());
    }

    public List<Delivery> getPastDeliveriesOfCustomer(final String customerId) {
        return this.deliveryRepository.getDeliveriesByCustomerId(customerId)
                .stream().filter(delivery -> !isActiveDelivery.test(delivery))
                .collect(Collectors.toList());
    }

    public Delivery createDelivery(final CreateDeliveryDto createDeliveryDto) {
        // TODO
        // Check that the box exists and get the box
        // Check that the box does not contain deliveries of any other user
        // make sure that deliverer exists and the given id has DELIVERER role @Valid?
        // make sure that customer exists and has CUSTOMER role

        // create delivery & send email to customer
        return null;
    }

    Predicate<Delivery> isActiveDelivery = delivery -> !delivery.getDeliveryStatus().equals(DeliveryStatus.DELIVERED);

    public List<Delivery> changeStatusToCollected(final DeliveryCollectedDto deliveryCollectedDto) {
        // TODO
        /*
           Validate that user with this id exists and has DELIVERER role
           For each delivery
            - Validate that delivery exists
            - Make sure that delivery state is created
            - Change status to collected
            - Send email to the customer
         */
        return Collections.singletonList(new Delivery());
    }

    public List<Delivery> changeStatusToDeposited(final DeliveryDepositedDto deliveryDepositedDto) {
        // TODO
        /*
        - Validate that delivery, deliverer & box exists
        - Make sure that the box with this id is the designated pickup box of delivery
        - Make sure that delivery state is Collected and the deliverer id is actually the deliverer of this delivery
        - Add the delivery to collectedDeliveries of respective box.
        - Change status to deposited
        - Send email to the customer
         */
        return new ArrayList<>();
    }

    public List<Delivery> changeStatusToDelivered(final DeliveryDeliveredDto deliveryDeliveredDto) {
        // TODO
        /*
        - All deliveries inside this box should be collected
        - Makes sure that this user has deliveries inside that box and therefore is allowed to open it. Otherwise rejects it.
        - System should make sure that all collected deliveries by this deliverer have been delivered.
        - Change status to delivered
        - Clear the containsDeliveries of Box.
         */
        return new ArrayList<>();
    }

}
