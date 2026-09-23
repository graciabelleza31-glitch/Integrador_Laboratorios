package com.proyecto.integrador.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardAlumController {
    @GetMapping("/dashboardAlum")
    public String mostrarDashboardAlumno(HttpSession session, Model model) {
        // Validar si hay sesión activa
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        model.addAttribute("nombre", session.getAttribute("nombreUsuario"));
        return "dashboardAlum"; // Apunta a src/main/resources/templates/dashboardAlum.html
    }
}
