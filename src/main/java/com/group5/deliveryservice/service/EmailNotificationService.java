package com.group5.deliveryservice.service;

import com.group5.deliveryservice.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.group5.deliveryservice.config.EmailConfig;

@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendCreatedDeliveryNotification(String email, String customerLastName, String trackingId) {
        SimpleMailMessage mailMessage = EmailConfig.createTemplate("ASEDelivery: New order created.");
        mailMessage.setText("Dear Mr./Ms. " + customerLastName + ",\nYour order is on the way. You can track it with the following tracking code:\n" + trackingId + "\n\nWith kind regards,\nASEDelivery");
        mailMessage.setTo(email);
        javaMailSender.send(mailMessage);
    }

    public void sendDepositedDeliveryEmailNotification(String email, String customerLastName) {
        SimpleMailMessage mailMessage = EmailConfig.createTemplate("ASEDelivery: Order collected");
        mailMessage.setText("Dear Mr./Ms. " + customerLastName + ",\nYour order was collected from the pick up box.\n\nWith kind regards,\nASEDelivery");
        mailMessage.setTo(email);
        javaMailSender.send(mailMessage);
    }

    public void sendDeliveredDeliveryEmailNotification(String email, String customerLastName) {
        SimpleMailMessage mailMessage = EmailConfig.createTemplate("ASEDelivery: Order delivered");
        mailMessage.setText("Dear Mr./Ms. " + customerLastName + ",\nYour order was delivered to a pick up box.\n\nWith kind regards,\nASEDelivery");
        mailMessage.setTo(email);
        javaMailSender.send(mailMessage);
    }

}
