package com.proyecto.integrador.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.integrador.modelo.ReservaIntegrante;
import com.proyecto.integrador.modelo.ReservaIntegranteId;

@Repository
public interface ReservaIntegranteRepository extends JpaRepository<ReservaIntegrante, ReservaIntegranteId> {

    List<ReservaIntegrante> findByReserva_IdReserva(String idReserva);

    List<ReservaIntegrante> findByAlumno_IdEstud(String idEstud);
}
