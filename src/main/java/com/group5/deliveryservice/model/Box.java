package com.group5.deliveryservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "boxes")
public class Box {

    @Id
    private String id;

    @Indexed(unique = true)
    private String stationName;

    private String stationAddress;

    private List<Delivery> containedDeliveries;
}
