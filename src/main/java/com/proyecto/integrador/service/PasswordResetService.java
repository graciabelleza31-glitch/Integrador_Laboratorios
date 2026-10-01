package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.modelo.PasswordResetToken;
import com.proyecto.integrador.repository.AlumnoRepository;
import com.proyecto.integrador.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private AlumnoRepository alumnoRepositorio;

    @Autowired
    private PasswordResetTokenRepository tokenRepositorio;

    @Autowired
    private EmailService emailService;

    /**
     * Paso 1: Genera un código, lo guarda en la BD y lo envía por correo.
     * 
     * @return El correo del alumno (enmascarado) o null si no existe.
     */
    @Transactional
    public String generarYEnviarCodigo(String idEstud) {
        Optional<Alumno> alumnoOpt = alumnoRepositorio.findById(idEstud.trim().toUpperCase());
        
        if (alumnoOpt.isEmpty()) {
            return null;
        }
        
        Alumno alumno = alumnoOpt.get();
        
        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));
        
        // Generar ID del token
        String idToken = "TKN" + (System.currentTimeMillis() % 10000000);
        
        // Crear el token
        PasswordResetToken token = new PasswordResetToken(
                idToken,
                alumno,
                codigo,
                LocalDateTime.now().plusMinutes(15),
                false
        );
        
        tokenRepositorio.save(token);
        
        // 🔑 CORREO INSTITUCIONAL: se construye desde el idEstud
        String correoDestino = "graciabelleza31@gmail.com";
System.out.println("🔧 Modo prueba: correo redirigido a " + correoDestino + " (destino real: " + alumno.getIdEstud().toLowerCase() + "@utp.edu.pe)");
        
        // Enviar correo
        emailService.enviarCodigoRestablecimiento(
                correoDestino,
                alumno.getNombre(),
                codigo
        );
        
        // Retornar el correo enmascarado
        return enmascararCorreo(correoDestino);
    }

    /**
     * Paso 2: Verifica el código y devuelve el token si es válido.
     */
    @Transactional(readOnly = true)
    public PasswordResetToken verificarCodigo(String codigo) {
        Optional<PasswordResetToken> tokenOpt = tokenRepositorio.findByCodigoTemporalAndUsadoFalse(codigo.trim());
        
        if (tokenOpt.isEmpty()) {
            return null;
        }
        
        PasswordResetToken token = tokenOpt.get();
        
        if (token.getFechaVencimiento().isBefore(LocalDateTime.now())) {
            return null;
        }
        
        return token;
    }

    /**
     * Paso 3: Cambia la contraseña del alumno y marca el token como usado.
     */
    @Transactional
    public boolean cambiarPassword(String idToken, String nuevaPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepositorio.findById(idToken);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken token = tokenOpt.get();
        
        if (token.getUsado() || token.getFechaVencimiento().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        Alumno alumno = token.getAlumno();
        alumno.setPassword(nuevaPassword);
        alumnoRepositorio.save(alumno);
        
        token.setUsado(true);
        tokenRepositorio.save(token);
        
        return true;
    }

    /**
     * Enmascara un correo: "u24201349@utp.edu.pe" → "u24*****@utp.edu.pe"
     */
    private String enmascararCorreo(String correo) {
        int arroba = correo.indexOf("@");
        if (arroba <= 3) return correo;
        
        String usuario = correo.substring(0, arroba);
        String dominio = correo.substring(arroba);
        
        String visible = usuario.substring(0, 3);
        String oculto = "*".repeat(Math.max(0, usuario.length() - 3));
        
        return visible + oculto + dominio;
    }
}
