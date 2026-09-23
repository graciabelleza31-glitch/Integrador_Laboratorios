package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    List<Producto> findByStockActualLessThan(Integer stockMinimo);
    List<Producto> findByCategoria(String categoria);

    // Buscar productos que se venden
    List<Producto> findByEsVenta(Boolean esVenta);
}
