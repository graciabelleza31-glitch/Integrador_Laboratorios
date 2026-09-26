package com.proyecto.integrador.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.integrador.modelo.Laboratorio;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, String> {

    List<Laboratorio> findByTipoIgnoreCase(String tipo);

    List<Laboratorio> findByUbicacionCubiculoStartingWith(String bloque);
}
