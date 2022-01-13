package com.group5.deliveryservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "deliveries")
public class Delivery {

    @Transient
    public static final String SEQUENCE_NAME = "deliveries_sequence";

    @Transient
    public static final String TRACKING_CODE_SEQUENCE_NAME = "tracking_code_sequence";

    @Id
    private long id;

    private long boxId;

    private long customerId;

    private long delivererId;

    @Indexed(unique = true)
    private String trackingId;

    private String description;

    private DeliveryStatus deliveryStatus;

    private boolean isActive;

    private Date assigned_at;

    private Date collected_at;

    private Date deposited_at;

    private Date delivered_at;

    public Delivery(long id, String trackingId, String description, DeliveryStatus deliveryStatus, boolean isActive) {
        this.id = id;
        this.trackingId = trackingId;
        this.description = description;
        this.deliveryStatus = deliveryStatus;
        this.isActive = isActive;
    }
}
