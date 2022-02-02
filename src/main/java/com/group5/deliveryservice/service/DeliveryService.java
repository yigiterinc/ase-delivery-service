package com.group5.deliveryservice.service;

import com.group5.deliveryservice.dto.*;
import com.group5.deliveryservice.exception.BoxAlreadyFullException;
import com.group5.deliveryservice.exception.InvalidIdException;
import com.group5.deliveryservice.exception.InvalidStatusChangeException;
import com.group5.deliveryservice.exception.WrongBoxDepositAttemptException;
import com.group5.deliveryservice.mail.MailService;
import com.group5.deliveryservice.mail.StatusChangeMailRequest;
import com.group5.deliveryservice.model.Box;
import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
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
        var boxIsValid = isBoxAvailableForNewDelivery(createDeliveryDto.getCustomerId(), createDeliveryDto.getBoxId());
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
        new Thread(() -> mailService.sendEmailTo(userMailAddress, statusChangeMailRequest)).start();

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

    public List<Delivery> changeStatusToCollected(final List<String> deliveryIds, String delivererId) {
        var deliveriesToSaveUpdates = new ArrayList<Delivery>();
        for (String deliveryId : deliveryIds) {
            Delivery updatedDelivery = null;
            try {
                updatedDelivery = changeSingleDeliveryStatusToCollected(deliveryId, delivererId);
            } catch (InvalidIdException invalidIdException) {
                System.out.println("Delivery or deliverer with id not found - deliveryId:" + deliveryId);
            } catch (InvalidStatusChangeException invalidStatusChangeException) {
                System.out.println("Invalid status change exception in delivery " + deliveryId);
            }

            if (updatedDelivery != null) {
                deliveriesToSaveUpdates.add(updatedDelivery);
            }
        }

        return deliveryRepository.saveAll(deliveriesToSaveUpdates);
    }

    public Delivery changeSingleDeliveryStatusToCollected(final String deliveryId, final String delivererId) {
        var delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(
                        () -> new InvalidIdException(
                                String.format("The delivery with id %s not found!", deliveryId)));

        if (!delivery.getDeliveryStatus().equals(DeliveryStatus.CREATED)) {
            throw new InvalidStatusChangeException();
        }

        var deliverer = getUserDetails(delivererId);
        if (!userHasExpectedRole(deliverer, "DELIVERER")) {
            throw new InvalidIdException(String.format("The deliverer with id %s not found!", delivererId));
        }

        if (!delivery.getDelivererId().equals(delivererId)) {
            throw new InvalidIdException("The deliverer id is not equal to delivererId of this delivery");
        }

        delivery.setDeliveryStatus(DeliveryStatus.COLLECTED);

        var userId = delivery.getCustomerId();
        var userDetails = getUserDetails(userId);
        var statusChangeMailRequest = new StatusChangeMailRequest(DeliveryStatus.COLLECTED, delivery.getId());
        new Thread(() -> mailService.sendEmailTo(userDetails.getEmail(), statusChangeMailRequest)).start();

        return delivery;
    }

    public Delivery changeStatusToDeposited(final String deliveryId, final String delivererId, final String boxId) {
        var delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(
                        () -> new InvalidIdException(
                                String.format("The delivery with id %s not found!", deliveryId)));

        if (!delivery.getDeliveryStatus().equals(DeliveryStatus.COLLECTED)) {
            throw new InvalidStatusChangeException();
        }

        if (!delivery.getDelivererId().equals(delivererId)) {
            throw new InvalidIdException("Supplied delivererId does not match the delivererId of this delivery");
        }

        var boxIsEqualToTargetBoxOfDelivery = boxId.equals(delivery.getTargetPickupBox().getId());
        if (!boxIsEqualToTargetBoxOfDelivery) {
            throw new WrongBoxDepositAttemptException();
        }

        var deliverer = getUserDetails(delivererId);
        if (!userHasExpectedRole(deliverer, "DELIVERER")) {
            throw new InvalidIdException(String.format("The deliverer with id %s not found!", delivererId));
        }

        delivery.setDeliveryStatus(DeliveryStatus.DEPOSITED);

        var userId = delivery.getCustomerId();
        var userDetails = getUserDetails(userId);
        var statusChangeMailRequest = new StatusChangeMailRequest(DeliveryStatus.DEPOSITED, delivery.getId());

        new Thread(() -> mailService.sendEmailTo(userDetails.getEmail(), statusChangeMailRequest)).start();

        return deliveryRepository.save(delivery);
    }

    public boolean isBoxAvailableForNewDelivery(final String customerId, final String boxId) {
        var box = boxService.findById(boxId);
        return boxOnlyContainsDeliveriesOfThisCustomer(box, customerId);
    }

    private boolean boxOnlyContainsDeliveriesOfThisCustomer(final Box box, final String customerId) {
        assert box != null;

        var deliveriesInBox = deliveryRepository
                .findAllByTargetPickupBoxIdAndDeliveryStatus(box.getId(), DeliveryStatus.DEPOSITED);
        var boxIsEmpty = deliveriesInBox.isEmpty();
        var containsDeliveriesOfThisCustomerOnly = deliveriesInBox
                .stream().allMatch(delivery -> delivery.getCustomerId().equals(customerId));

        return boxIsEmpty || containsDeliveriesOfThisCustomerOnly;
    }

    public List<Delivery> changeStatusToDelivered(final String userId, final String boxId) {
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
