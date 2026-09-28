package com.proyecto.integrador.controller;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.repository.LaboratorioRepository;
import com.proyecto.integrador.repository.ProductoRepository;
import com.proyecto.integrador.repository.SancionRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/inventario")
public class InventarioAdminController {

    @Autowired
    private ProductoRepository productoRepositorio;

    @Autowired
    private LaboratorioRepository laboratorioRepositorio;

    @Autowired
    private SancionRepository sancionRepositorio;

    @GetMapping
    @Transactional(readOnly = true)
    public String verInventario(HttpSession sesion, Model modelo) {
        List<Producto> productos = productoRepositorio.findAll();

        // Categorías únicas para los filtros
        List<String> categorias = productos.stream()
                .map(Producto::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();

        long totalItems = productos.size();
        long stockCritico = productos.stream()
                .filter(p -> p.getStockActual() != null && p.getStockActual() <= 10)
                .count();

        long incidenciasAbiertas = sancionRepositorio.count();

        // Lista fija con los 3 únicos tipos de laboratorio
        List<String> tiposLaboratorios = List.of("Química", "Física", "Mecatrónica");

        modelo.addAttribute("productos", productos);
        modelo.addAttribute("tiposLaboratorios", tiposLaboratorios);
        modelo.addAttribute("categorias", categorias);
        modelo.addAttribute("totalItems", totalItems);
        modelo.addAttribute("stockCritico", stockCritico);
        modelo.addAttribute("totalIncidencias", incidenciasAbiertas > 0 ? incidenciasAbiertas : 1);

        return "admin-inventario";
    }

    @PostMapping("/actualizar-stock/{idProducto}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> actualizarStock(
            @PathVariable("idProducto") String idProducto,
            @RequestParam("cantidad") int cantidad) {

        Optional<Producto> prodOpt = productoRepositorio.findById(idProducto);
        if (prodOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Producto no encontrado"));
        }

        Producto p = prodOpt.get();
        int stockActual = (p.getStockActual() != null) ? p.getStockActual() : 0;
        int nuevoStock = Math.max(0, stockActual + cantidad);
        p.setStockActual(nuevoStock);
        productoRepositorio.save(p);

        return ResponseEntity.ok(Collections.singletonMap("nuevoStock", nuevoStock));
    }

    @PostMapping("/guardar")
    @Transactional
    public String guardarProducto(
            @RequestParam("idProducto") String idProducto,
            @RequestParam("nombre") String nombre,
            @RequestParam("categoria") String categoria,
            @RequestParam("stockActual") Integer stockActual,
            @RequestParam(value = "costoReposicion", required = false) BigDecimal costoReposicion,
            @RequestParam(value = "tipoLab", required = false) String tipoLab) {

        Producto nuevo = new Producto();
        nuevo.setIdProducto(idProducto.trim().toUpperCase());
        nuevo.setNombre(nombre.trim());
        nuevo.setCategoria(categoria.trim());
        nuevo.setStockActual(stockActual != null ? stockActual : 0);

        if (costoReposicion != null && costoReposicion.compareTo(BigDecimal.ZERO) > 0) {
            nuevo.setCostoReposicion(costoReposicion);
        } else {
            nuevo.setCostoReposicion(BigDecimal.ZERO);
        }

        // Asignamos el primer laboratorio disponible que coincida con ese tipo
        if (tipoLab != null && !tipoLab.isBlank()) {
            List<Laboratorio> labs = laboratorioRepositorio.findAll();
            for (Laboratorio l : labs) {
                if (l.getTipo() != null && l.getTipo().equalsIgnoreCase(tipoLab)) {
                    nuevo.setLaboratorio(l);
                    break;
                }
            }
        }

        productoRepositorio.save(nuevo);

        return "redirect:/admin/inventario";
    }
}
