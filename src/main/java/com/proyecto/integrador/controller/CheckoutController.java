package com.proyecto.integrador.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.modelo.ReservaIntegrante;
import com.proyecto.integrador.modelo.ReservaItem;
import com.proyecto.integrador.repository.ReservaItemRepository;
import com.proyecto.integrador.repository.ReservaRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/checkout")
public class CheckoutController {

    @Autowired
    private ReservaRepository reservaRepositorio;

    @Autowired
    private ReservaItemRepository reservaItemRepositorio;

    @GetMapping
    @Transactional(readOnly = true)
    public String mostrarCheckout(HttpSession sesion, Model modelo) {
        Object usuario = sesion.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Reserva> reservasEnUso = reservaRepositorio.findByEstado("EN_USO");
        List<TarjetaCheckoutDto> listaTarjetas = new ArrayList<>();

        for (Reserva r : reservasEnUso) {
            String responsable = "Sin asignar";
            if (r.getIntegrantes() != null && !r.getIntegrantes().isEmpty()) {
                ReservaIntegrante primero = r.getIntegrantes().get(0);
                if (primero.getAlumno() != null) {
                    responsable = primero.getAlumno().getNombre() + " " + primero.getAlumno().getApellido();
                }
            }

            // Consulta directa de ítems para evitar MultipleBagFetchException
            List<ReservaItem> itemsReserva = reservaItemRepositorio.findByReserva_IdReserva(r.getIdReserva());
            int totalItems = 0;
            if (itemsReserva != null) {
                for (ReservaItem item : itemsReserva) {
                    totalItems += (item.getCantidad() != null) ? item.getCantidad() : 1;
                }
            }

            String nombreLab = (r.getLaboratorio() != null) ? r.getLaboratorio().getNombre() : "Laboratorio";
            String cubiculo = (r.getLaboratorio() != null) ? r.getLaboratorio().getUbicacionCubiculo() : "Sin cubículo";
            String horario = r.getHoraInicio() + "–" + r.getHoraFin();

            listaTarjetas.add(new TarjetaCheckoutDto(
                    r.getIdReserva(),
                    nombreLab,
                    cubiculo,
                    horario,
                    responsable,
                    totalItems
            ));
        }

        modelo.addAttribute("reservas", listaTarjetas);
        return "checkout";
    }

    @PostMapping("/confirmar")
    @Transactional
    public String procesarDevolucion(@RequestParam("idReserva") String idReserva) {
        Optional<Reserva> reservaOpcional = reservaRepositorio.findById(idReserva);
        if (reservaOpcional.isPresent()) {
            Reserva reserva = reservaOpcional.get();
            reserva.setEstado("DEVUELTA");
            reservaRepositorio.save(reserva);
        }
        return "redirect:/admin/checkout";
    }

    public static class TarjetaCheckoutDto {

        private String idReserva;
        private String laboratorio;
        private String cubiculo;
        private String horario;
        private String responsable;
        private int totalItems;

        public TarjetaCheckoutDto(String idReserva, String laboratorio, String cubiculo, String horario, String responsable, int totalItems) {
            this.idReserva = idReserva;
            this.laboratorio = laboratorio;
            this.cubiculo = cubiculo;
            this.horario = horario;
            this.responsable = responsable;
            this.totalItems = totalItems;
        }

        public String getIdReserva() {
            return idReserva;
        }

        public String getLaboratorio() {
            return laboratorio;
        }

        public String getCubiculo() {
            return cubiculo;
        }

        public String getHorario() {
            return horario;
        }

        public String getResponsable() {
            return responsable;
        }

        public int getTotalItems() {
            return totalItems;
        }
    }
}
