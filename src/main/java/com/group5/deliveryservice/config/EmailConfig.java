package com.group5.deliveryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    private static final String serverEmail = "asedelivery5@gmail.com";
    private static final String serverEmailPassword = "lwpdegumbenshfrz";

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(serverEmail);
        mailSender.setPassword(serverEmailPassword);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");

        return mailSender;
    }

    public static SimpleMailMessage createTemplate(String subject) {
        SimpleMailMessage template = new SimpleMailMessage();
        template.setFrom(serverEmail);
        template.setSubject(subject);
        return template;
    }
}
