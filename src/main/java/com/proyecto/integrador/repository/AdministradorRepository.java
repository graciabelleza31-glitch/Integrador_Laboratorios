package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {
    Optional<Administrador> findByDni(String dni);
}
