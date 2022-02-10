package com.group5.deliveryservice.service;

import com.group5.deliveryservice.dto.CreateBoxDto;
import com.group5.deliveryservice.dto.DelivererAssignedBoxDto;
import com.group5.deliveryservice.exception.InvalidIdException;
import com.group5.deliveryservice.model.Box;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.repository.BoxRepository;
import com.group5.deliveryservice.repository.DeliveryRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BoxService {

    private final BoxRepository boxRepository;
    private final DeliveryRepository deliveryRepository;

    public BoxService(BoxRepository boxRepository, DeliveryRepository deliveryRepository) {
        this.boxRepository = boxRepository;
        this.deliveryRepository = deliveryRepository;
    }

    public Box createBox(final CreateBoxDto boxDto) {
        Box box = new Box(boxDto.getStationName(), boxDto.getStationAddress());
        return boxRepository.save(box);
    }

    public void deleteBox(String boxId) {
        boxRepository.deleteById(boxId);
    }

    public Box findById(String id) {
        return boxRepository.findById(id)
                .orElseThrow(
                        () -> new InvalidIdException(
                                String.format("Box with id %s not found", id)));
    }

    public List<DelivererAssignedBoxDto> getDelivererAssignedBoxes(String delivererId) {
        return deliveryRepository.findAllByDelivererId(delivererId)
                .stream()
                .map(delivery -> new DelivererAssignedBoxDto(findById(delivery.getTargetPickupBox().getId()), delivery.getDeliveryStatus()))
                .collect(Collectors.toList());
    }

    public void checkNameUniqueness(CreateBoxDto createBoxDto) throws RuntimeException {
        if (boxRepository.findByStationName(createBoxDto.getStationName()).isPresent())
            throw new RuntimeException("Box with name " + createBoxDto.getStationName() + " already exists");
    }

    public Box updateBox(String boxId, CreateBoxDto boxDetails) {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        checkNameUniqueness(boxDetails);
        box.setStationName(boxDetails.getStationName());
        box.setStationAddress(boxDetails.getStationAddress());

        return boxRepository.save(box);
    }

    public List<Box> findAll() {
        return boxRepository.findAll();
    }
}
