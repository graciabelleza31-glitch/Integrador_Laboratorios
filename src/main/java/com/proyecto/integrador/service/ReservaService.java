package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.modelo.ReservaItem;
import com.proyecto.integrador.repository.LaboratorioRepository;
import com.proyecto.integrador.repository.ReservaItemRepository;
import com.proyecto.integrador.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaItemRepository reservaItemRepository;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    /**
     * Obtiene todas las reservas del sistema.
     */
    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    /**
     * Busca una reserva por su ID.
     */
    public Optional<Reserva> buscarPorId(String idReserva) {
        return reservaRepository.findById(idReserva);
    }

    /**
     * Obtiene las reservas de una fecha específica.
     */
    public List<Reserva> listarPorFecha(LocalDate fecha) {
        return reservaRepository.findByFechaReserva(fecha);
    }

    /**
     * Obtiene las reservas de hoy.
     */
    public List<Reserva> listarDeHoy() {
        return reservaRepository.findByFechaReserva(LocalDate.now());
    }

    /**
     * Obtiene las reservas por estado.
     */
    public List<Reserva> listarPorEstado(String estado) {
        return reservaRepository.findByEstado(estado);
    }

    /**
     * Obtiene las reservas de un laboratorio específico.
     */
    public List<Reserva> listarPorLaboratorio(String idLab) {
        return reservaRepository.findByLaboratorioIdLab(idLab);
    }

    /**
     * Crea una nueva reserva.
     * Por defecto, el estado es "PENDIENTE".
     */
    public Reserva crearReserva(Reserva reserva) {
        if (reserva.getEstado() == null) {
            reserva.setEstado("PENDIENTE");
        }
        return reservaRepository.save(reserva);
    }

    /**
     * Confirma una reserva (cambia el estado de PENDIENTE a CONFIRMADA).
     */
    public Reserva confirmarReserva(String idReserva) {
        Optional<Reserva> opt = reservaRepository.findById(idReserva);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            reserva.setEstado("CONFIRMADA");
            return reservaRepository.save(reserva);
        }
        return null;
    }

    /**
     * Hace check-in de una reserva (cambia el estado a EN_USO).
     */
    public Reserva hacerCheckIn(String idReserva) {
        Optional<Reserva> opt = reservaRepository.findById(idReserva);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            reserva.setEstado("EN_USO");
            return reservaRepository.save(reserva);
        }
        return null;
    }

    /**
     * Hace check-out de una reserva (cambia el estado a FINALIZADA).
     */
    public Reserva hacerCheckOut(String idReserva) {
        Optional<Reserva> opt = reservaRepository.findById(idReserva);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            reserva.setEstado("FINALIZADA");
            return reservaRepository.save(reserva);
        }
        return null;
    }

    /**
     * Cancela una reserva (cambia el estado a CANCELADA).
     */
    public Reserva cancelarReserva(String idReserva) {
        Optional<Reserva> opt = reservaRepository.findById(idReserva);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            reserva.setEstado("CANCELADA");
            return reservaRepository.save(reserva);
        }
        return null;
    }

    /**
     * Guarda o actualiza una reserva.
     */
    public Reserva guardar(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    /**
     * Elimina una reserva por su ID.
     */
    public void eliminar(String idReserva) {
        reservaRepository.deleteById(idReserva);
    }

    /**
     * Verifica si un laboratorio está disponible en una fecha y hora específicas.
     * Devuelve true si NO hay otra reserva en el mismo horario.
     */
    public boolean estaDisponible(String idLab, LocalDate fecha, 
                                java.time.LocalTime horaInicio, 
                                java.time.LocalTime horaFin) {
        List<Reserva> reservas = reservaRepository.findByLaboratorioIdLab(idLab);
        for (Reserva r : reservas) {
            if (r.getFechaReserva().equals(fecha)) {
                // Verificar si hay solapamiento de horarios
                boolean solapa = !horaFin.isBefore(r.getHoraInicio()) 
                            && !horaInicio.isAfter(r.getHoraFin());
                if (solapa && !"CANCELADA".equals(r.getEstado())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Cuenta las reservas en un estado específico.
     */
    public long contarPorEstado(String estado) {
        return reservaRepository.findByEstado(estado).size();
    }

    /**
     * Obtiene los items (equipos) de una reserva.
     */
    public List<ReservaItem> obtenerItemsDeReserva(String idReserva) {
        return reservaItemRepository.findByReservaIdReserva(idReserva);
    }
}
