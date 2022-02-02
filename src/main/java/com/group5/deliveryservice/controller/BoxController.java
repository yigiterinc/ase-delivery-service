package com.group5.deliveryservice.controller;

import com.group5.deliveryservice.dto.CreateBoxDto;
import com.group5.deliveryservice.model.Box;
import com.group5.deliveryservice.model.Delivery;
import com.group5.deliveryservice.model.DeliveryStatus;
import com.group5.deliveryservice.repository.BoxRepository;
import com.group5.deliveryservice.repository.DeliveryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/boxes")
public class BoxController {

    private final BoxRepository boxRepository;
    private final DeliveryRepository deliveryRepository;

    public BoxController(BoxRepository boxRepository, DeliveryRepository deliveryRepository) {
        this.boxRepository = boxRepository;
        this.deliveryRepository = deliveryRepository;
    }

    private void checkNameUniqueness(CreateBoxDto createBoxDto) throws RuntimeException {
        if (boxRepository.findByStationName(createBoxDto.getStationName()).isPresent())
            throw new RuntimeException("Box with name " + createBoxDto.getStationName() + " already exists");
    }

    @GetMapping("/all")
    public List<Box> getAllBoxes() {
        return boxRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Box> getBoxById(@PathVariable(value = "id") String boxId)
            throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        return ResponseEntity.ok().body(box);
    }

    @GetMapping("/deliverer/{delivererId}")
    public ResponseEntity<List<Box>> getBoxByDelivererId(@PathVariable String delivererId)
            throws RuntimeException {
        List<String> boxIds = deliveryRepository.findAllByDeliveryStatusAndDelivererId(DeliveryStatus.CREATED, delivererId)
                .stream()
                .map(Delivery::getTargetPickupBox)
                .distinct()
                .map(Box::getId)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(boxRepository.findByIdIn(boxIds));
    }

    @PostMapping
    public Box createBox(@RequestBody CreateBoxDto boxDto) {
        checkNameUniqueness(boxDto);
        Box box = new Box(boxDto.getStationName(), boxDto.getStationAddress());
        return boxRepository.save(box);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Box> updateBox(@PathVariable(value = "id") String boxId,
                                         @Valid @RequestBody CreateBoxDto boxDetails) throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        checkNameUniqueness(boxDetails);
        box.setStationName(boxDetails.getStationName());
        box.setStationAddress(boxDetails.getStationAddress());
        return ResponseEntity.ok(boxRepository.save(box));
    }

    @DeleteMapping("/boxes/{id}")
    public Map<String, Boolean> deleteBox(@PathVariable(value = "id") String boxId)
            throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        boxRepository.delete(box);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
