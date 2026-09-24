package com.proyecto.integrador.controller;

import com.proyecto.integrador.modelo.Administrador;
import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class DashboardAdminController {
    @Autowired
    private AdministradorService administradorService;

    @GetMapping("/dashboard-admin")
    public String mostrarDashboard(
            @RequestParam(required = false) String idAdmin,
            Model model) {

        // Datos del administrador (si viene el idAdmin)
        if (idAdmin != null) {
            Optional<Administrador> adminOpt = administradorService.obtenerAdministrador(idAdmin);
            adminOpt.ifPresent(admin -> model.addAttribute("administrador", admin));
        }

        // KPIs
        Map<String, Object> kpis = administradorService.obtenerKPIs();
        model.addAttribute("labsEnUso", kpis.get("labsEnUso"));
        model.addAttribute("labsDisponibles", kpis.get("labsDisponibles"));
        model.addAttribute("pendientesCheckIn", kpis.get("pendientesCheckIn"));
        model.addAttribute("prestamosActivos", kpis.get("prestamosActivos"));
        model.addAttribute("incidentesAbiertos", kpis.get("incidentesAbiertos"));

        // Laboratorios
        List<Laboratorio> laboratorios = administradorService.obtenerLaboratorios();
        model.addAttribute("laboratorios", laboratorios);

        // Agenda de hoy
        List<Reserva> agenda = administradorService.obtenerAgendaDeHoy();
        model.addAttribute("agenda", agenda);

        // Alertas de stock
        List<Producto> productosStockBajo = administradorService.obtenerProductosConStockBajo();
        model.addAttribute("productosStockBajo", productosStockBajo);

        return "admin/dashboard"; // Apunta a templates/admin/dashboard.html
    }
}
