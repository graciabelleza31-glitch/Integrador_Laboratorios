package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.*;
import com.proyecto.integrador.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdministradorService {
    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    public Optional<Administrador> obtenerAdministrador(String idAdmin) {
        return administradorRepository.findById(idAdmin);
    }
    public Map<String, Object> obtenerKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        // Reservas de hoy
        List<Reserva> reservasHoy = reservaRepository.findByFechaReserva(LocalDate.now());

        // Labs en uso hoy
        long labsEnUso = reservasHoy.stream()
                .filter(r -> "EN_USO".equals(r.getEstado()))
                .count();
        kpis.put("labsEnUso", labsEnUso);

        // Labs disponibles (total en el sistema)
        kpis.put("labsDisponibles", laboratorioRepository.count());

        // Pendientes check-in (reservas confirmadas hoy)
        long pendientes = reservasHoy.stream()
                .filter(r -> "CONFIRMADA".equals(r.getEstado()))
                .count();
        kpis.put("pendientesCheckIn", pendientes);

        // Préstamos activos (reservas en uso con items no devueltos)
        long prestamosActivos = reservasHoy.stream()
                .filter(r -> "EN_USO".equals(r.getEstado()))
                .count();
        kpis.put("prestamosActivos", prestamosActivos);

        // Incidentes abiertos (sanciones pendientes)
        kpis.put("incidentesAbiertos", 1); // TODO: implementar cuando tengamos sanciones

        return kpis;
    }

    /**
     * Obtiene la lista de todos los laboratorios.
     */
    public List<Laboratorio> obtenerLaboratorios() {
        return laboratorioRepository.findAll();
    }

    /**
     * Obtiene las reservas de hoy ordenadas por hora de inicio.
     */
    public List<Reserva> obtenerAgendaDeHoy() {
        return reservaRepository.findByFechaReserva(LocalDate.now());
    }

    /**
     * Obtiene los productos con stock bajo (menor a 10).
     */
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findByStockActualLessThan(10);
    }

    /**
     * Obtiene todos los alumnos registrados.
     */
    public List<Alumno> obtenerTodosLosAlumnos() {
        return alumnoRepository.findAll();
    }

    /**
     * Obtiene todas las reservas del sistema.
     */
    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    /**
     * Cuenta cuántos laboratorios están en un estado específico (para el dashboard).
     */
    public long contarLabsPorEstado(String estado) {
        List<Reserva> reservasHoy = reservaRepository.findByFechaReserva(LocalDate.now());
        return reservasHoy.stream()
                .filter(r -> estado.equals(r.getEstado()))
                .count();
    }
}
