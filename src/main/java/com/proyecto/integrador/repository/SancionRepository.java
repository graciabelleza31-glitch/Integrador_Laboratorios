package com.proyecto.integrador.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.integrador.modelo.Sancion;

@Repository
public interface SancionRepository extends JpaRepository<Sancion, String> {

    // Devuelve las sanciones de un alumno según su estado (ej: "PENDIENTE")
    List<Sancion> findByAlumno_IdEstudAndEstado(String idEstud, String estado);

    // Consulta rápida si tiene alguna sanción pendiente (true/false)
    boolean existsByAlumno_IdEstudAndEstado(String idEstud, String estado);
}
