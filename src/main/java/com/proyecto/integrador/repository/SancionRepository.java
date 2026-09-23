package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SancionRepository extends JpaRepository<Sancion, String> {
}
