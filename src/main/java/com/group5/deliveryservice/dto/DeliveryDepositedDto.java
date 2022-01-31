package com.group5.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeliveryDepositedDto implements DeliveryStatusUpdate {
    private String deliveryId;
    private String delivererId;
    private String boxId;
}
