package com.proyecto.integrador.repository;

import com.proyecto.integrador.modelo.ReservaItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaItemRepository extends JpaRepository<ReservaItem, Long> {
    List<ReservaItem> findByReservaIdReserva(String idReserva);

    // Buscar items por producto
    List<ReservaItem> findByProductoIdProducto(String idProducto);

    // Buscar items no devueltos
    List<ReservaItem> findByDevuelto(Boolean devuelto);
}
