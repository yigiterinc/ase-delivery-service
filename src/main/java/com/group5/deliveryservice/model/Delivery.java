package com.group5.deliveryservice.model;

import com.group5.deliveryservice.dto.CreateDeliveryDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "deliveries")
public class Delivery {

    @Id
    private String id;

    @NotNull
    @Indexed(unique = true)
    private String customerId;

    @NotNull
    private Box targetPickupBox;

    @NotNull
    @Indexed(unique = true)
    private String delivererId;

    private DeliveryStatus deliveryStatus;

    private Date assigned_at;

    private Date collected_at;

    private Date deposited_at;

    private Date delivered_at;

    public Delivery(String customerId, Box targetPickupBox, String delivererId) {
        this.customerId = customerId;
        this.targetPickupBox = targetPickupBox;
        this.delivererId = delivererId;
    }
}
