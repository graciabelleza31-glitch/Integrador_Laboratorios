package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, String> {
}
