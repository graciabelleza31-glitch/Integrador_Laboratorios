package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @Column(name = "id_producto", length = 6)
    private String idProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    @Column(name = "es_venta", nullable = false)
    private Boolean esVenta;

    @Column(name = "precio_venta")
    private Double precioVenta;

    @Column(name = "costo_reposicion")
    private Double costoReposicion;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;
}
