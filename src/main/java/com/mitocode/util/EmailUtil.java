package com.mitocode.util;

import com.mitocode.dto.request.communication.NotificationMessageRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendMail(NotificationMessageRequest request) throws MessagingException {
        try {
            // Configurar el contexto del template
            Context context = new Context();
            context.setVariables(request.getModel());

            // Determinar la plantilla a utilizar
            String template = "email/email-template"; // Plantilla por defecto
            if ("COMPLAINT-EMAIL".equalsIgnoreCase(request.getType()) || "CORRESPONDENCE".equalsIgnoreCase(request.getType())) {
                template = "email/complaint-email"; // Nueva plantilla para quejas y sugerencias
            }
            //log.info("Correos a enviar: {}", request.getTo());

            String html = templateEngine.process(template, context);

            // Crear el correo
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(request.getTo().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            helper.setText(html, true);
            helper.setFrom(request.getFrom());

            // Enviar el correo
            mailSender.send(message);
            System.out.println("Correo enviado con éxito.");

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al enviar el correo.");
        }
    }
}

            /*
             📌 Agregar adjuntos si existen
            List<ByteArrayDataSource> attachments = request.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                for (ByteArrayDataSource attachment : attachments) {
                    helper.addAttachment(attachment.getName(), attachment);
                }
            }
             */
