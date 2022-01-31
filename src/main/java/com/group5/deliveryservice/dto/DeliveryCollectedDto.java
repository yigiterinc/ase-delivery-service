package com.group5.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeliveryCollectedDto implements DeliveryStatusUpdate {
    private String delivererId;
    private List<String> deliveryIds;
}
