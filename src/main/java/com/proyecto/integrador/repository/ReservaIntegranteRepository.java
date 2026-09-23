package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.ReservaIntegrante;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaIntegranteRepository extends JpaRepository<ReservaIntegrante, Long> {
    List<ReservaIntegrante> findByReservaIdReserva(String idReserva);
    
    List<ReservaIntegrante> findByAlumnoIdEstud(String idEstud);
}
