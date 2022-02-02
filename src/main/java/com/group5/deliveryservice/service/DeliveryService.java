package com.group5.deliveryservice.service;

import com.group5.deliveryservice.dto.*;
import com.group5.deliveryservice.exception.BoxAlreadyFullException;
import com.group5.deliveryservice.exception.InvalidIdException;
import com.group5.deliveryservice.mail.MailService;
import com.group5.deliveryservice.mail.StatusChangeMailRequest;
import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final BoxService boxService;
    private final MailService mailService;

    private final RestTemplate restTemplate;

    private final String CUSTOMER_AUTHENTICATION_SERVICE_BASE_URL = "http://customer-authentication-service:8081/api/cas";
    private final Function<String, String> getCustomerAuthenticationServiceFetchUserUrl = id -> CUSTOMER_AUTHENTICATION_SERVICE_BASE_URL + "/users/" + id;

    public DeliveryService(final DeliveryRepository deliveryRepository,
                           final BoxService boxService,
                           final MailService mailService,
                           final RestTemplate restTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.boxService = boxService;
        this.mailService = mailService;
        this.restTemplate = restTemplate;
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
        // Check that the box does not contain deliveries of any other user
        var boxIsValid = boxService.isBoxAvailableForNewDelivery(createDeliveryDto.getCustomerId(), createDeliveryDto.getBoxId());
        if (!boxIsValid) {
            throw new BoxAlreadyFullException();
        }

        // Validate role of user with CAS
        var userId = createDeliveryDto.getCustomerId();
        var userDetails = getUserDetails(userId);
        if (!userHasExpectedRole(userDetails, "CUSTOMER")) {
            throw new InvalidIdException(String.format("The customer with id %s not found!", userId));
        }

        var delivererId = createDeliveryDto.getDelivererId();
        var delivererDetails = getUserDetails(delivererId);
        // Validate role of deliverer with CAS
        if (!userHasExpectedRole(delivererDetails, "DELIVERER")) {
            throw new InvalidIdException(String.format("The deliverer with id %s not found!", delivererId));
        }

        var box = boxService.findById(createDeliveryDto.getBoxId());
        var delivery = deliveryRepository.save(
                new Delivery(createDeliveryDto.getCustomerId(), box, createDeliveryDto.getDelivererId()));


        var userMailAddress = userDetails.getEmail();
        var statusChangeMailRequest = new StatusChangeMailRequest(DeliveryStatus.CREATED, delivery.getId());
        mailService.sendEmailTo(userMailAddress, statusChangeMailRequest);

        return delivery;
    }

    private boolean userHasExpectedRole(UserDto userDto, String expectedRole) {
        return userDto != null && expectedRole.equals(userDto.getRole());
    }

    private UserDto getUserDetails(String userId) {
        var url = getCustomerAuthenticationServiceFetchUserUrl.apply(userId);
        return restTemplate.getForObject(url, UserDto.class);
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
