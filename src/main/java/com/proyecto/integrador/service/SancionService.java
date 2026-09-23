package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Sancion;
import com.proyecto.integrador.repository.SancionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SancionService {
    @Autowired
    private SancionRepository sancionRepository;

    /**
     * Obtiene todas las sanciones.
     */
    public List<Sancion> listarTodas() {
        return sancionRepository.findAll();
    }

    /**
     * Busca una sanción por su ID.
     */
    public Optional<Sancion> buscarPorId(String idSancion) {
        return sancionRepository.findById(idSancion);
    }

    /**
     * Obtiene las sanciones de una reserva específica.
     */
    public List<Sancion> listarPorReserva(String idReserva) {
        return sancionRepository.findByReservaIdReserva(idReserva);
    }

    /**
     * Obtiene las sanciones por estado (PENDIENTE, PAGADA).
     */
    public List<Sancion> listarPorEstado(String estado) {
        return sancionRepository.findByEstado(estado);
    }

    /**
     * Obtiene todas las sanciones pendientes de pago.
     */
    public List<Sancion> listarPendientes() {
        return sancionRepository.findByEstado("PENDIENTE");
    }

    /**
     * Crea una nueva sanción.
     * Por defecto, el estado es "PENDIENTE".
     */
    public Sancion crearSancion(Sancion sancion) {
        if (sancion.getEstado() == null) {
            sancion.setEstado("PENDIENTE");
        }
        return sancionRepository.save(sancion);
    }

    /**
     * Marca una sanción como pagada.
     */
    public Sancion marcarComoPagada(String idSancion) {
        Optional<Sancion> opt = sancionRepository.findById(idSancion);
        if (opt.isPresent()) {
            Sancion sancion = opt.get();
            sancion.setEstado("PAGADA");
            return sancionRepository.save(sancion);
        }
        return null;
    }

    /**
     * Guarda o actualiza una sanción.
     */
    public Sancion guardar(Sancion sancion) {
        return sancionRepository.save(sancion);
    }

    /**
     * Elimina una sanción por su ID.
     */
    public void eliminar(String idSancion) {
        sancionRepository.deleteById(idSancion);
    }

    /**
     * Cuenta las sanciones pendientes de pago.
     */
    public long contarPendientes() {
        return sancionRepository.findByEstado("PENDIENTE").size();
    }
}
