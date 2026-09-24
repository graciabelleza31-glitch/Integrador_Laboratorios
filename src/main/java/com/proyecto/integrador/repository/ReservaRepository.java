package com.proyecto.integrador.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.integrador.modelo.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {

    // Método que necesitan CheckoutController e InspeccionController:
    List<Reserva> findByEstado(String estado);

    // Método para consultar horarios ocupados por laboratorio y fecha:
    List<Reserva> findByLaboratorio_IdLabAndFechaReserva(String idLab, LocalDate fechaReserva);

    // Consulta para obtener el último ID correlativo de reserva (ej: RES005):
    @Query(value = "SELECT id_reserva FROM reserva WHERE id_reserva LIKE 'RES%' ORDER BY id_reserva DESC LIMIT 1", nativeQuery = true)
    Optional<String> findUltimoIdReserva();
}
