package com.group5.deliveryservice.controller;

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
@RequestMapping("/api/boxes")
public class BoxController {

    private final BoxRepository boxRepository;

    // TODO: Should not access the repository directly
    final DeliveryRepository deliveryRepository;

    public BoxController(BoxRepository boxRepository, DeliveryRepository deliveryRepository) {
        this.boxRepository = boxRepository;
        this.deliveryRepository = deliveryRepository;
    }

    private void checkNameUniqueness(Box box) throws RuntimeException {
        if (boxRepository.findByName(box.getName()).isPresent())
            throw new RuntimeException("Box with name " + box.getName() + " already exists");
    }

    @GetMapping("/all")
    public List<Box> getAllBoxes() {
        return boxRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Box> getBoxById(@PathVariable(value = "id") Long boxId)
            throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        return ResponseEntity.ok().body(box);
    }

    @GetMapping("/deliverer/{delivererId}")
    public ResponseEntity<List<Box>> getBoxByDelivererId(@PathVariable long delivererId)
            throws RuntimeException {
        List<Long> boxIds = deliveryRepository.findAllByDeliveryStatusAndDelivererId(DeliveryStatus.CREATED, delivererId)
                .stream()
                .map(Delivery::getBoxId).distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(boxRepository.findByIdIn(boxIds));
    }

    @PostMapping
    public Box createBox(@Valid @RequestBody Box box) {
        checkNameUniqueness(box);
        return boxRepository.save(box);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Box> updateBox(@PathVariable(value = "id") Long boxId,
                                         @Valid @RequestBody Box boxDetails) throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        checkNameUniqueness(boxDetails);
        box.setName(boxDetails.getName());
        box.setAddress(boxDetails.getAddress());
        return ResponseEntity.ok(boxRepository.save(box));
    }

    @DeleteMapping("/boxes/{id}")
    public Map<String, Boolean> deleteBox(@PathVariable(value = "id") Long boxId)
            throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        boxRepository.delete(box);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
