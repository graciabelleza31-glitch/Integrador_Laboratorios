package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Sancion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SancionRepository extends JpaRepository<Sancion, String> {
    List<Sancion> findByReservaIdReserva(String idReserva);
    
    // Buscar sanciones por estado (PENDIENTE, PAGADA)
    List<Sancion> findByEstado(String estado);

}
