package com.proyecto.integrador.controller;

import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    /**
     * Muestra la página de Inventario.
     */
    @GetMapping("/inventario")
    public String mostrarInventario(Model model) {
        List<Producto> productos = productoService.listarTodos();
        List<Producto> stockBajo = productoService.obtenerProductosConStockBajo(10);
        
        model.addAttribute("productos", productos);
        model.addAttribute("productosStockBajo", stockBajo);
        return "admin/inventario";
    }

    /**
     * Ajusta el stock de un producto (suma o resta).
     * cantidad > 0 → suma
     * cantidad < 0 → resta
     */
    @PostMapping("/inventario/ajustar")
    public String ajustarStock(
            @RequestParam String idProducto,
            @RequestParam Integer cantidad) {
        productoService.actualizarStock(idProducto, cantidad);
        return "redirect:/inventario";
    }
}
