package com.proyecto.integrador.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.integrador.modelo.Administrador;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {

    Optional<Administrador> findByDni(String dni);

    boolean existsByIdAdmin(String idAdmin);
}
