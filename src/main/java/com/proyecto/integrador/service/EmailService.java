package com.proyecto.integrador.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarCodigoRestablecimiento(String destinatario, String nombreAlumno, String codigo) {
        String url = "https://api.brevo.com/v3/smtp/email";

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        // Sender
        Map<String, Object> sender = new HashMap<>();
        sender.put("name", fromName);
        sender.put("email", fromEmail);

        // To
        Map<String, Object> to = new HashMap<>();
        to.put("email", destinatario);
        to.put("name", nombreAlumno);

        // Body
        String cuerpo = "Hola " + nombreAlumno + ",\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña en UTP +Lab.\n\n"
                + "Tu código de verificación es: " + codigo + "\n\n"
                + "Este código expira en 15 minutos. Si no solicitaste este cambio, ignora este mensaje.\n\n"
                + "Atentamente,\n"
                + "Equipo UTP +Lab";

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(to));
        body.put("subject", "UTP +Lab - Código para restablecer contraseña");
        body.put("textContent", cuerpo);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Correo enviado a: " + destinatario + " (status: " + response.getStatusCode() + ")");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo con Brevo: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}