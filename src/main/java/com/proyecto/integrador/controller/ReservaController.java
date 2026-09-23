package com.proyecto.integrador.controller;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.service.ReservaService;
import com.proyecto.integrador.service.LaboratorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reservas")
public class ReservaController {
    @Autowired
    private ReservaService reservaService;

    @Autowired
    private LaboratorioService laboratorioService;

    // Vista principal para listar o crear reserva
    @GetMapping
    public String listarReservas(Model model) {
        model.addAttribute("listaReservas", reservaService.listarTodas());
        model.addAttribute("laboratorios", laboratorioService.listarTodos());
        return "reservas/lista"; // Apunta a src/main/resources/templates/reservas/lista.html
    }

    // Formulario para nueva reserva
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevaReserva(Model model) {
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("laboratorios", laboratorioService.listarTodos());
        return "reservas/form-reserva";
    }

    // Procesar la creación de la reserva
    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute("reserva") Reserva reserva) {
        reservaService.guardar(reserva);
        return "redirect:/reservas";
    }
}
