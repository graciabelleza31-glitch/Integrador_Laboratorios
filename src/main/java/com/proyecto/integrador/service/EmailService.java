package com.proyecto.integrador.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía un correo con el código de restablecimiento de contraseña.
     */
    public void enviarCodigoRestablecimiento(String destinatario, String nombreAlumno, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(fromEmail);
        mensaje.setTo(destinatario);
        mensaje.setSubject("UTP +Lab - Código para restablecer contraseña");
        
        String cuerpo = "Hola " + nombreAlumno + ",\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña en UTP +Lab.\n\n"
                + "Tu código de verificación es: " + codigo + "\n\n"
                + "Este código expira en 15 minutos. Si no solicitaste este cambio, ignora este mensaje.\n\n"
                + "Atentamente,\n"
                + "Equipo UTP +Lab";
        
        mensaje.setText(cuerpo);
        
        try {
            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}
