package com.group5.deliveryservice.repository;

import com.group5.deliveryservice.model.Box;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxRepository extends MongoRepository<Box, Long> {

    Optional<Box> findByName(String name);

    List<Box> findByIdIn(List<Long> ids);
}
