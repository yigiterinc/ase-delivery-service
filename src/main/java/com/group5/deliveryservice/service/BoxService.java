package com.group5.deliveryservice.service;

import com.group5.deliveryservice.exception.InvalidIdException;
import com.group5.deliveryservice.model.Box;
import com.group5.deliveryservice.repository.BoxRepository;
import org.springframework.stereotype.Service;

@Service
public class BoxService {

    private final BoxRepository boxRepository;

    public BoxService(BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    public Box findById(String id) {
        return boxRepository.findById(id)
                .orElseThrow(
                        () -> new InvalidIdException(
                                String.format("Box with id %s not found", id)));
    }

}
