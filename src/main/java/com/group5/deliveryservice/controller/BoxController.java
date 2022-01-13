package com.group5.deliveryservice.controller;

import com.group5.deliveryservice.model.Box;
import com.group5.deliveryservice.repository.BoxRepository;
import com.group5.deliveryservice.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BoxController {

    @Autowired
    private BoxRepository boxRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    private void checkNameUniqueness(Box box) throws RuntimeException {
        if (boxRepository.findByName(box.getName()).isPresent())
            throw new RuntimeException("Box with name " + box.getName() + " already exists");
    }

    private ResponseEntity<Box> updateBox(Box box1, Box box2) {
        checkNameUniqueness(box2);
        box1.setName(box2.getName());
        box1.setAddress(box2.getAddress());
        return ResponseEntity.ok(boxRepository.save(box1));
    }

    public Map<String, Boolean> deleteBox(Box box) {
        boxRepository.delete(box);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @GetMapping("/boxes/all")
    public List<Box> getAllBoxes() {
        return boxRepository.findAll();
    }

    @GetMapping("/boxes/{id}")
    public ResponseEntity<Box> getBoxById(@PathVariable(value = "id") Long boxId)
            throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        return ResponseEntity.ok().body(box);
    }

    @GetMapping("/boxes")
    public ResponseEntity<Box> getBoxByName(@RequestParam(value = "name") String name)
            throws RuntimeException {
        Box box = boxRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Box not found for name " + name));
        return ResponseEntity.ok().body(box);
    }

    @PostMapping("/boxes")
    public Box createBox(@Valid @RequestBody Box box) {
        checkNameUniqueness(box);
        box.setId(sequenceGeneratorService.generateSequence(Box.SEQUENCE_NAME));
        return boxRepository.save(box);
    }

    @PutMapping("/boxes/{id}")
    public ResponseEntity<Box> updateBox(@PathVariable(value = "id") Long boxId,
                                         @Valid @RequestBody Box boxDetails) throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        return updateBox(box, boxDetails);
    }

    @PutMapping("/boxes")
    public ResponseEntity<Box> updateBox(@RequestParam(value = "name") String name,
                                           @Valid @RequestBody Box boxDetails) throws RuntimeException {
        Box box = boxRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Box not found for name " + name));
        return updateBox(box, boxDetails);
    }

    @DeleteMapping("/boxes/{id}")
    public Map<String, Boolean> deleteBox(@PathVariable(value = "id") Long boxId)
            throws RuntimeException {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box not found for id " + boxId));
        return deleteBox(box);
    }

    @DeleteMapping("/boxes")
    public Map<String, Boolean> deleteBox(@RequestParam(value = "name") String name)
            throws RuntimeException {
        Box box = boxRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Box not found for name " + name));
        return deleteBox(box);
    }
}
