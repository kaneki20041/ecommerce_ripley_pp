package com.proyecto.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.proyecto.model.Usuario;
import com.proyecto.repositories.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserRepository userRepository;

    public void sendReactivationEmail(Usuario usuario, String couponCode, int discountPercentage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(usuario.getEmail());
            helper.setSubject("¡Te extrañamos, " + usuario.getFirstName() + "! Aquí tienes un regalo especial 🎁");
            helper.setFrom("joseandi20041@gmail.com");

            Context context = new Context();
            context.setVariable("firstName", usuario.getFirstName());
            context.setVariable("couponCode", couponCode);
            context.setVariable("discountPercentage", discountPercentage);

            // Process HTML template
            String htmlContent = templateEngine.process("reactivation-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            // Update user record
            usuario.setLastReactivationEmailSent(LocalDateTime.now());
            userRepository.save(usuario);

            System.out.println("Reactivation email sent to " + usuario.getEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send reactivation email to " + usuario.getEmail());
            e.printStackTrace();
        }
    }
}
