package com.proyecto.integrador.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.integrador.modelo.Alumno;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, String> {

    Optional<Alumno> findByDni(String dni);

    boolean existsByIdEstud(String idEstud);
}
