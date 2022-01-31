package com.group5.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeliveryDeliveredDto implements DeliveryStatusUpdate {
    private String userId;
    private String boxId;
}
