package com.proyecto.integrador.controller;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ReservaController {
    @Autowired
    private ReservaService reservaService;

    /**
     * Muestra la página de Check-in.
     * Lista las reservas pendientes (CONFIRMADA o PENDIENTE) de hoy.
     */
    @GetMapping("/check-in")
    public String mostrarCheckIn(Model model) {
        List<Reserva> reservasHoy = reservaService.listarDeHoy();
        
        // Filtrar solo las que están pendientes de check-in
        List<Reserva> pendientes = reservasHoy.stream()
                .filter(r -> "CONFIRMADA".equals(r.getEstado()) || "PENDIENTE".equals(r.getEstado()))
                .toList();
        
        model.addAttribute("reservasPendientes", pendientes);
        return "admin/check-in";
    }

    /**
     * Procesa el check-in de una reserva.
     * Cambia el estado de la reserva a "EN_USO".
     */
    @PostMapping("/check-in/iniciar")
    public String iniciarCheckIn(@RequestParam String idReserva) {
        reservaService.hacerCheckIn(idReserva);
        return "redirect:/check-in?exito=true";
    }

    /**
     * Muestra la página de Check-out.
     * Lista las reservas en uso (EN_USO) que necesitan check-out.
     */
    @GetMapping("/check-out")
    public String mostrarCheckOut(Model model) {
        List<Reserva> reservasHoy = reservaService.listarDeHoy();
        
        // Filtrar solo las que están EN_USO
        List<Reserva> enUso = reservasHoy.stream()
                .filter(r -> "EN_USO".equals(r.getEstado()))
                .toList();
        
        model.addAttribute("reservasEnUso", enUso);
        return "admin/check-out";
    }

    /**
     * Procesa el check-out de una reserva.
     * Cambia el estado de la reserva a "FINALIZADA".
     */
    @PostMapping("/check-out/finalizar")
    public String finalizarCheckOut(@RequestParam String idReserva) {
        reservaService.hacerCheckOut(idReserva);
        return "redirect:/check-out?exito=true";
    }
}
