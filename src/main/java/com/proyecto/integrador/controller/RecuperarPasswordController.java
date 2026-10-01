package com.proyecto.integrador.controller;

import com.proyecto.integrador.modelo.PasswordResetToken;
import com.proyecto.integrador.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecuperarPasswordController {

    @Autowired
    private PasswordResetService passwordResetService;

    // ============ PASO 1: Ingresar código UTP ============
    @GetMapping("/recuperar/restablecer")
    public String mostrarRestablecer() {
        System.out.println("🔵 [DEBUG] Entrando a /recuperar/restablecer");
    String resultado = "recuperar/restablecer";
    System.out.println("🔵 [DEBUG] Retornando vista: " + resultado);
        return "recuperar/restablecer";
    }

    @PostMapping("/recuperar/restablecer")
    public String enviarCodigo(@RequestParam("codigoUtp") String codigoUtp, Model modelo) {
        String correoEnmascarado = passwordResetService.generarYEnviarCodigo(codigoUtp);
        
        if (correoEnmascarado == null) {
            modelo.addAttribute("error", "No encontramos ningún estudiante con ese código UTP.");
            modelo.addAttribute("codigoIngresado", codigoUtp);
            return "recuperar/restablecer";
        }
        
        modelo.addAttribute("correoEnmascarado", correoEnmascarado);
        modelo.addAttribute("codigoUtp", codigoUtp);
        return "recuperar/verificar-codigo";
    }

    // ============ PASO 2: Verificar código ============
    @PostMapping("/recuperar/verificar-codigo")
    public String verificarCodigo(
            @RequestParam("codigo") String codigo,
            @RequestParam("codigoUtp") String codigoUtp,
            Model modelo) {
        
        PasswordResetToken token = passwordResetService.verificarCodigo(codigo);
        
        if (token == null) {
            modelo.addAttribute("error", "El código es incorrecto o ya expiró. Intenta de nuevo.");
            modelo.addAttribute("codigoUtp", codigoUtp);
            String correo = codigoUtp.toLowerCase() + "@utp.edu.pe";
            modelo.addAttribute("correoEnmascarado", correo);
            return "recuperar/verificar-codigo";
        }
        
        modelo.addAttribute("idToken", token.getIdToken());
        return "recuperar/nueva-password";
    }

    // ============ PASO 3: Cambiar contraseña ============
    @PostMapping("/recuperar/cambiar-password")
    public String cambiarPassword(
            @RequestParam("idToken") String idToken,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            Model modelo) {
        
        if (!password.equals(passwordConfirm)) {
            modelo.addAttribute("error", "Las contraseñas no coinciden.");
            modelo.addAttribute("idToken", idToken);
            return "recuperar/nueva-password";
        }
        
        if (password.length() < 6) {
            modelo.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            modelo.addAttribute("idToken", idToken);
            return "recuperar/nueva-password";
        }
        
        boolean exito = passwordResetService.cambiarPassword(idToken, password);
        
        if (!exito) {
            modelo.addAttribute("error", "Hubo un problema. Intenta de nuevo.");
            modelo.addAttribute("idToken", idToken);
            return "recuperar/nueva-password";
        }
        
        return "redirect:/login?reset=true";
    }
}
