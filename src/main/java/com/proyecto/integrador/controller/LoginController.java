package com.proyecto.integrador.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.repository.AlumnoRepository;
import com.proyecto.integrador.repository.LaboratorioRepository;
import com.proyecto.integrador.service.UsuarioService;
import com.proyecto.integrador.service.UsuarioService.ResultadoLogin;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioServicio;

    @Autowired
    private AlumnoRepository alumnoRepositorio;

    // ⭐ NUEVO: Inyectamos LaboratorioRepository para verificar si la carrera tiene laboratorios asignados
    @Autowired
    private LaboratorioRepository laboratorioRepositorio;

    @GetMapping({"/", "/login"})
    public String mostrarLogin(HttpSession sesion) {
        Object usuarioActivo = sesion.getAttribute("usuarioLogueado");
        if (usuarioActivo != null) {
            String rol = (String) sesion.getAttribute("rol");
            return "ADMIN".equalsIgnoreCase(rol) ? "redirect:/admin/dashboard" : "redirect:/alumno/laboratorios";
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

            if (resultado.esAdmin()) {
                sesion.setAttribute("nombreUsuario", resultado.getNombreCompleto());
                sesion.setAttribute("codigoUtp", resultado.getCodigo());
                sesion.setAttribute("rol", resultado.getRol());
                sesion.setAttribute("usuarioLogueado", resultado);
                return "redirect:/admin/dashboard";
            } else {
                // Buscamos al alumno para revisar su carrera ANTES de crearle sesión
                Alumno alumno = alumnoRepositorio.findById(resultado.getCodigo()).orElse(null);

                if (alumno == null || alumno.getCarrera() == null) {
                    throw new RuntimeException("Acceso denegado: No tienes una carrera registrada en el sistema.");
                }

                String idCarrera = alumno.getCarrera().getIdCarrera();
                String nombreCarrera = alumno.getCarrera().getNombre();
                Boolean permiteLab = alumno.getCarrera().getPermiteLab();

                // 1. Mini filtro por columna permite_lab de la tabla carrera
                if (Boolean.FALSE.equals(permiteLab)) {
                    throw new RuntimeException("Usted no tiene permitido el ingreso porque la carrera de " 
                            + nombreCarrera + " no requiere uso de laboratorios especializados.");
                }

                // 2. Mini filtro por tabla carrera_permiso_lab (verifica que tenga al menos 1 laboratorio asignado)
                List<Laboratorio> labsPermitidos = laboratorioRepositorio.findLaboratoriosPermitidosPorCarrera(idCarrera);
                if (labsPermitidos == null || labsPermitidos.isEmpty()) {
                    throw new RuntimeException("Usted no tiene permitido el ingreso porque su carrera (" 
                            + nombreCarrera + ") no cuenta con laboratorios habilitados para reserva.");
                }

                // Si pasó el filtro de carrera, recién guardamos todo en la sesión y lo dejamos entrar
                sesion.setAttribute("nombreUsuario", resultado.getNombreCompleto());
                sesion.setAttribute("codigoUtp", resultado.getCodigo());
                sesion.setAttribute("rol", resultado.getRol());
                sesion.setAttribute("usuarioLogueado", alumno);
                sesion.setAttribute("idCarrera", idCarrera);
                sesion.setAttribute("nombreCarrera", nombreCarrera);
                sesion.setAttribute("permiteLab", true);

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

    @GetMapping("/alumno/panel")
    public String panelAlumno(HttpSession sesion) {
        return "redirect:/alumno/laboratorios";
    }
}