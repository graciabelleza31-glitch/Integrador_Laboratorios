package com.proyecto.integrador.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.modelo.ReservaItem;
import com.proyecto.integrador.modelo.Sancion;
import com.proyecto.integrador.repository.ProductoRepository;
import com.proyecto.integrador.repository.ReservaItemRepository;
import com.proyecto.integrador.repository.ReservaRepository;
import com.proyecto.integrador.repository.SancionRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/inspeccion")
public class InspeccionController {

    @Autowired
    private ReservaRepository reservaRepositorio;

    @Autowired
    private ReservaItemRepository reservaItemRepositorio;

    @Autowired
    private ProductoRepository productoRepositorio;

    @Autowired
    private SancionRepository sancionRepositorio;

    @GetMapping
    @Transactional(readOnly = true)
    public String verInspecciones(HttpSession sesion, Model modelo) {
        Object usuario = sesion.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Busca reservas devueltas pendientes de inspeccionar
        List<Reserva> pendientes = reservaRepositorio.findByEstado("DEVUELTA");
        List<TarjetaInspeccionDto> lista = new ArrayList<>();

        for (Reserva r : pendientes) {
            int integrantesCount = (r.getIntegrantes() != null) ? r.getIntegrantes().size() : 0;

            List<ReservaItem> items = reservaItemRepositorio.findByReserva_IdReserva(r.getIdReserva());
            int totalItems = 0;
            List<ItemDetalleDto> itemsDto = new ArrayList<>();

            if (items != null) {
                for (ReservaItem it : items) {
                    totalItems += (it.getCantidad() != null) ? it.getCantidad() : 1;
                    Producto p = it.getProducto();
                    BigDecimal costo = (p.getCostoReposicion() != null) ? p.getCostoReposicion() : BigDecimal.ZERO;

                    itemsDto.add(new ItemDetalleDto(
                            p.getIdProducto(),
                            p.getNombre(),
                            p.getCategoria(),
                            costo,
                            it.getCantidad()
                    ));
                }
            }

            String labNombre = (r.getLaboratorio() != null) ? r.getLaboratorio().getNombre() : "Laboratorio";
            String cubiculo = (r.getLaboratorio() != null) ? r.getLaboratorio().getUbicacionCubiculo() : "S/C";
            String horario = r.getHoraInicio() + "–" + r.getHoraFin();

            lista.add(new TarjetaInspeccionDto(
                    r.getIdReserva(),
                    labNombre,
                    cubiculo,
                    horario,
                    integrantesCount,
                    totalItems,
                    itemsDto
            ));
        }

        modelo.addAttribute("reservas", lista);
        return "inspeccion";
    }

    @PostMapping("/finalizar")
    @Transactional
    public String finalizarInspeccion(
            @RequestParam("idReserva") String idReserva,
            @RequestParam Map<String, String> parametros) {

        Optional<Reserva> reservaOpt = reservaRepositorio.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            return "redirect:/admin/inspeccion";
        }

        Reserva reserva = reservaOpt.get();
        List<ReservaItem> items = reservaItemRepositorio.findByReserva_IdReserva(idReserva);

        Alumno responsable = null;
        if (reserva.getIntegrantes() != null && !reserva.getIntegrantes().isEmpty()) {
            responsable = reserva.getIntegrantes().get(0).getAlumno();
        }

        for (ReservaItem item : items) {
            String idProd = item.getProducto().getIdProducto();
            String estado = parametros.get("estado_" + idProd);

            if ("CONFORME".equalsIgnoreCase(estado)) {
                item.setDevuelto(true);
                // Si está en buen estado: SE SUMA A LA BASE DE DATOS
                Producto prod = item.getProducto();
                prod.setStockActual(prod.getStockActual() + item.getCantidad());
                productoRepositorio.save(prod);
            } else if ("DANADO".equalsIgnoreCase(estado)) {
                item.setDevuelto(false);
                // Si está dañado: NO SE SUMA AL STOCK Y PASA A INCIDENCIAS / SANCIÓN
                Producto prod = item.getProducto();
                String motivo = parametros.get("descripcion_" + idProd);
                if (motivo == null || motivo.trim().isEmpty()) {
                    motivo = "Material entregado dañado durante la práctica";
                }

                String costoStr = parametros.get("costo_" + idProd);
                BigDecimal monto = (prod.getCostoReposicion() != null) ? prod.getCostoReposicion() : BigDecimal.ZERO;
                try {
                    if (costoStr != null && !costoStr.trim().isEmpty()) {
                        monto = new BigDecimal(costoStr.trim());
                    }
                } catch (Exception ignored) {
                }

                String idSancion = "S" + (System.currentTimeMillis() % 10000);
                Sancion sancion = new Sancion(
                        idSancion,
                        reserva,
                        responsable,
                        prod,
                        motivo,
                        monto,
                        "PENDIENTE"
                );
                sancionRepositorio.save(sancion);
            }
            reservaItemRepositorio.save(item);
        }

        // Termina la inspección
        reserva.setEstado("INSPECCIONADA");
        reservaRepositorio.save(reserva);

        return "redirect:/admin/inspeccion";
    }

    // DTOs auxiliares
    public static class TarjetaInspeccionDto {

        private String idReserva;
        private String laboratorio;
        private String cubiculo;
        private String horario;
        private int integrantes;
        private int totalItems;
        private List<ItemDetalleDto> items;

        public TarjetaInspeccionDto(String idReserva, String laboratorio, String cubiculo, String horario, int integrantes, int totalItems, List<ItemDetalleDto> items) {
            this.idReserva = idReserva;
            this.laboratorio = laboratorio;
            this.cubiculo = cubiculo;
            this.horario = horario;
            this.integrantes = integrantes;
            this.totalItems = totalItems;
            this.items = items;
        }

        public String getIdReserva() {
            return idReserva;
        }

        public String getLaboratorio() {
            return laboratorio;
        }

        public String getCubiculo() {
            return cubiculo;
        }

        public String getHorario() {
            return horario;
        }

        public int getIntegrantes() {
            return integrantes;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public List<ItemDetalleDto> getItems() {
            return items;
        }
    }

    public static class ItemDetalleDto {

        private String idProducto;
        private String nombre;
        private String categoria;
        private BigDecimal costoReposicion;
        private int cantidad;

        public ItemDetalleDto(String idProducto, String nombre, String categoria, BigDecimal costoReposicion, int cantidad) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.categoria = categoria;
            this.costoReposicion = costoReposicion;
            this.cantidad = cantidad;
        }

        public String getIdProducto() {
            return idProducto;
        }

        public String getNombre() {
            return nombre;
        }

        public String getCategoria() {
            return categoria;
        }

        public BigDecimal getCostoReposicion() {
            return costoReposicion;
        }

        public int getCantidad() {
            return cantidad;
        }
    }
}
