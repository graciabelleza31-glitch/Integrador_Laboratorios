package com.proyecto.integrador.controller;

import com.proyecto.integrador.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class LoginController {
    @Autowired 
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String MostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String codigoUtp,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        UsuarioService.LoginResult resultado= usuarioService.validarCredenciales(codigoUtp, password);
        if (resultado != null) {
            // Guardamos la información del usuario en la sesión HTTP
            session.setAttribute("usuarioLogueado", resultado);
            session.setAttribute("nombreUsuario", resultado.getNombre());
            session.setAttribute("codigoUtp", codigoUtp);

            if (resultado.esAdmin()) {
                session.setAttribute("rol", "ADMIN");
                return "redirect:/dashboardAdmi";
            } else {
                session.setAttribute("rol", "ALUMNO");
                return "redirect:/dashboardAlum";
            }
        } else {
            model.addAttribute("error", "Código UTP o contraseña incorrectos");
            return "login";
        }
        
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/login";
    } 

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Cierra la sesión activa
        return "redirect:/login";
    }
}
