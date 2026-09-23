package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface CarreraRepository extends JpaRepository<Carrera, String> {
}
