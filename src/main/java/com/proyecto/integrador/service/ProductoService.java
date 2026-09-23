package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Obtiene todos los productos.
     */
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    /**
     * Busca un producto por su ID.
     */
    public Optional<Producto> buscarPorId(String idProducto) {
        return productoRepository.findById(idProducto);
    }

    /**
     * Busca productos por categoría.
     */
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    /**
     * Obtiene los productos con stock bajo (menor al mínimo indicado).
     */
    public List<Producto> obtenerProductosConStockBajo(Integer stockMinimo) {
        return productoRepository.findByStockActualLessThan(stockMinimo);
    }

    /**
     * Guarda o actualiza un producto.
     */
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    /**
     * Elimina un producto por su ID.
     */
    public void eliminar(String idProducto) {
        productoRepository.deleteById(idProducto);
    }

    /**
     * Actualiza el stock de un producto (suma o resta).
     */
    public Producto actualizarStock(String idProducto, Integer cantidad) {
        Optional<Producto> opt = productoRepository.findById(idProducto);
        if (opt.isPresent()) {
            Producto producto = opt.get();
            producto.setStockActual(producto.getStockActual() + cantidad);
            return productoRepository.save(producto);
        }
        return null;
    }
}
