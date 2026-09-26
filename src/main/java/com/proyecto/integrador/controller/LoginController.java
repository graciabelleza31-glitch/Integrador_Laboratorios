package com.proyecto.integrador.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.repository.AlumnoRepository;
import com.proyecto.integrador.service.UsuarioService;
import com.proyecto.integrador.service.UsuarioService.ResultadoLogin;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioServicio;

    @Autowired
    private AlumnoRepository alumnoRepositorio;

    @GetMapping({"/", "/login"})
    public String mostrarLogin(HttpSession sesion) {
        Object usuarioActivo = sesion.getAttribute("usuarioLogueado");
        if (usuarioActivo != null) {
            String rol = (String) sesion.getAttribute("rol");
            return "ADMIN".equalsIgnoreCase(rol) ? "redirect:/dashboardAdmin" : "redirect:/alumno/laboratorios";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("codigoUtp") String codigoUtp,
            @RequestParam("password") String password,
            HttpSession sesion,
            Model modelo) {
        try {
            ResultadoLogin resultado = usuarioServicio.validarCredenciales(codigoUtp, password);

            sesion.setAttribute("nombreUsuario", resultado.getNombreCompleto());
            sesion.setAttribute("codigoUtp", resultado.getCodigo());
            sesion.setAttribute("rol", resultado.getRol());

            if (resultado.esAdmin()) {
                sesion.setAttribute("usuarioLogueado", resultado);
                return "redirect:/dashboardAdmin";
            } else {
                // Guarda la entidad Alumno completa en sesión para AlumnoPortalController
                Alumno alumno = alumnoRepositorio.findById(resultado.getCodigo()).orElse(null);
                sesion.setAttribute("usuarioLogueado", alumno);
                // REDIRIGE DIRECTO A LABORATORIOS:
                return "redirect:/alumno/laboratorios";
            }

        } catch (RuntimeException excepcion) {
            modelo.addAttribute("error", excepcion.getMessage());
            modelo.addAttribute("codigoIngresado", codigoUtp);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dashboardAdmin")
    public String panelAdministrador(HttpSession sesion, Model modelo) {
        Object usuario = sesion.getAttribute("usuarioLogueado");
        String rol = (String) sesion.getAttribute("rol");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(rol)) {
            return "redirect:/login";
        }
        modelo.addAttribute("usuario", usuario);
        return "dashboard-admin";
    }

    // Si alguien entra o recarga /dashboardAlum, lo mandamos a laboratorios
    @GetMapping("/dashboardAlum")
    public String panelAlumno(HttpSession sesion) {
        return "redirect:/alumno/laboratorios";
    }
}
