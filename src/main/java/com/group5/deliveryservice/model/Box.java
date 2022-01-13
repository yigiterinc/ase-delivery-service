package com.group5.deliveryservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "boxes")
public class Box {
    @Transient
    public static final String SEQUENCE_NAME = "boxes_sequence";

    @Id
    private long id;

    @Indexed(unique = true)
    private String name;

    private String address;
}
