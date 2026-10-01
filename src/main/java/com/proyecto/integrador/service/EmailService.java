package com.proyecto.integrador.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public void enviarCodigoRestablecimiento(String destinatario, String nombreAlumno, String codigo) {
        Resend resend = new Resend(resendApiKey);

        String cuerpo = "Hola " + nombreAlumno + ",\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña en UTP +Lab.\n\n"
                + "Tu código de verificación es: " + codigo + "\n\n"
                + "Este código expira en 15 minutos. Si no solicitaste este cambio, ignora este mensaje.\n\n"
                + "Atentamente,\n"
                + "Equipo UTP +Lab";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(destinatario)
                .subject("UTP +Lab - Código para restablecer contraseña")
                .text(cuerpo)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println("✅ Correo enviado a: " + destinatario + " (id: " + data.getId() + ")");
        } catch (ResendException e) {
            System.err.println("❌ Error al enviar correo con Resend: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}