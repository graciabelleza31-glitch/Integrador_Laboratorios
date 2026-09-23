package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {
    List<Reserva> findByFechaReserva(LocalDate fecha);
    List<Reserva> findByEstado(String estado);
}
