package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, String> {
}  

