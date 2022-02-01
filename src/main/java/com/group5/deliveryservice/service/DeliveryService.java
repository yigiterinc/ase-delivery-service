package com.group5.deliveryservice.service;

import com.group5.deliveryservice.dto.CreateDeliveryDto;
import com.group5.deliveryservice.dto.DeliveryCollectedDto;
import com.group5.deliveryservice.dto.DeliveryDeliveredDto;
import com.group5.deliveryservice.dto.DeliveryDepositedDto;
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

    private final String CUSTOMER_AUTHENTICATION_SERVICE_BASE_URL = "http://customer-authentication-service/api/cas";
    private final Function<String, String> getCustomerAuthenticationServiceFetchRoleUrl = id -> CUSTOMER_AUTHENTICATION_SERVICE_BASE_URL + "/" + id + "role";

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
        if (!userHasExpectedRole(userId, "CUSTOMER")) {
            throw new InvalidIdException(String.format("The customer with id %s not found!", userId));
        }

        var delivererId = createDeliveryDto.getDelivererId();
        // Validate role of deliverer with CAS
        if (!userHasExpectedRole(delivererId, "DELIVERER")) {
            throw new InvalidIdException(String.format("The deliverer with id %s not found!", delivererId));
        }

        var box = boxService.findById(createDeliveryDto.getBoxId());
        var delivery = deliveryRepository.save(
                new Delivery(createDeliveryDto.getCustomerId(), box, createDeliveryDto.getDelivererId()));

        var userMailAddress = "";   // TODO: Get the complete user object instead of just the role so we get the mail
        var statusChangeMailRequest = new StatusChangeMailRequest(DeliveryStatus.CREATED, delivery.getId());
        mailService.sendEmailTo(userMailAddress, statusChangeMailRequest);

        return delivery;
    }

    /**
     *
     * @param expectedRole: Role in String, "DISPATCHER", "DELIVERER" or "CUSTOMER"
     * @return whether the user's actual role matches the expected role
     */
    private boolean userHasExpectedRole(String userId, String expectedRole) {
        var url = getCustomerAuthenticationServiceFetchRoleUrl.apply(userId);
        var userRole = restTemplate.postForObject(url, userId, String.class);

        return expectedRole.equals(userRole);
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
