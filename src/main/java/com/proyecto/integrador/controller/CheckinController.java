package com.proyecto.integrador.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.modelo.ReservaIntegrante;
import com.proyecto.integrador.modelo.ReservaItem;
import com.proyecto.integrador.repository.ReservaIntegranteRepository;
import com.proyecto.integrador.repository.ReservaItemRepository;
import com.proyecto.integrador.repository.ReservaRepository;

@Controller
@RequestMapping("/admin/checkin")
public class CheckinController {

    @Autowired
    private ReservaRepository reservaRepositorio;

    @Autowired
    private ReservaIntegranteRepository reservaIntegranteRepositorio;

    @Autowired
    private ReservaItemRepository reservaItemRepositorio;

    // 1. Vista principal de Check-in
    @GetMapping
    @Transactional(readOnly = true)
    public String verCheckin(Model modelo) {
        // Obtenemos reservas CONFIRMADA o PENDIENTE
        List<Reserva> reservasConfirmadas = reservaRepositorio.findByEstado("CONFIRMADA");
        List<Reserva> reservasPendientes = reservaRepositorio.findByEstado("PENDIENTE");

        List<Reserva> todas = new ArrayList<>();
        if (reservasConfirmadas != null) {
            todas.addAll(reservasConfirmadas);
        }
        if (reservasPendientes != null) {
            todas.addAll(reservasPendientes);
        }

        List<CheckinReservaDto> listaCheckin = new ArrayList<>();

        for (Reserva r : todas) {
            List<ReservaIntegrante> integrantes = reservaIntegranteRepositorio.findByReserva_IdReserva(r.getIdReserva());
            List<String> nombresIntegrantes = new ArrayList<>();
            for (ReservaIntegrante ri : integrantes) {
                if (ri.getAlumno() != null) {
                    nombresIntegrantes.add(ri.getAlumno().getNombre() + " " + ri.getAlumno().getApellido());
                }
            }

            String labNombre = (r.getLaboratorio() != null) ? r.getLaboratorio().getNombre() : "Laboratorio";
            String cubiculo = (r.getLaboratorio() != null) ? r.getLaboratorio().getUbicacionCubiculo() : "S/C";
            String tipoLab = (r.getLaboratorio() != null && r.getLaboratorio().getTipo() != null) ? r.getLaboratorio().getTipo() : "Química";

            listaCheckin.add(new CheckinReservaDto(
                    r.getIdReserva(),
                    labNombre,
                    tipoLab,
                    cubiculo,
                    (r.getFechaReserva() != null ? r.getFechaReserva().toString() : ""),
                    r.getHoraInicio() + "–" + r.getHoraFin(),
                    r.getEstado(),
                    nombresIntegrantes
            ));
        }

        modelo.addAttribute("reservas", listaCheckin);
        return "admin-checkin";
    }

    // 2. Endpoint API JSON para cargar datos de la reserva al abrir el modal
    @GetMapping("/api/{idReserva}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> obtenerDetalleModal(@PathVariable("idReserva") String idReserva) {
        Optional<Reserva> resOpt = reservaRepositorio.findById(idReserva);
        if (resOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reserva r = resOpt.get();
        List<ReservaIntegrante> integrantes = reservaIntegranteRepositorio.findByReserva_IdReserva(idReserva);
        List<ReservaItem> items = reservaItemRepositorio.findByReserva_IdReserva(idReserva);

        List<IntegranteDetalleDto> listaEstudiantes = new ArrayList<>();
        for (ReservaIntegrante ri : integrantes) {
            if (ri.getAlumno() != null) {
                String nombreCompleto = ri.getAlumno().getNombre() + " " + ri.getAlumno().getApellido();
                String iniciales = ri.getAlumno().getNombre().substring(0, 1) + ri.getAlumno().getApellido().substring(0, 1);
                listaEstudiantes.add(new IntegranteDetalleDto(ri.getAlumno().getIdEstud(), nombreCompleto, iniciales.toUpperCase()));
            }
        }

        List<ItemDetalleDto> listaInsumos = new ArrayList<>();
        List<ItemDetalleDto> listaEpps = new ArrayList<>();

        for (ReservaItem it : items) {
            if (it.getProducto() != null) {
                String cat = (it.getProducto().getCategoria() != null) ? it.getProducto().getCategoria().trim().toUpperCase() : "";
                String nombre = it.getProducto().getNombre();
                String unidad = "und";
                if (cat.contains("SOLVENTE") || cat.contains("REACTIVO")) {
                    unidad = "mL";
                } else if (cat.contains("POLVO")) {
                    unidad = "g";
                }

                if (cat.contains("EPP")) {
                    listaEpps.add(new ItemDetalleDto(it.getProducto().getIdProducto(), nombre, "und", "🥼"));
                } else {
                    listaInsumos.add(new ItemDetalleDto(it.getProducto().getIdProducto(), nombre, unidad, "📦"));
                }
            }
        }

        // Si no seleccionó EPPs de costo, la bata y gafas básicas siempre van por protocolo
        if (listaEpps.isEmpty()) {
            listaEpps.add(new ItemDetalleDto("EPP-DEF-1", "Bata de laboratorio", "und", "🥼"));
            listaEpps.add(new ItemDetalleDto("EPP-DEF-2", "Gafas de seguridad", "und", "🥽"));
        }

        DetalleModalCheckinDto response = new DetalleModalCheckinDto(
                r.getIdReserva(),
                (r.getLaboratorio() != null ? r.getLaboratorio().getNombre() : "Laboratorio"),
                r.getHoraInicio() + "–" + r.getHoraFin(),
                listaEstudiantes,
                listaInsumos,
                listaEpps
        );

        return ResponseEntity.ok(response);
    }

    // 3. Confirmar Check-in: Responde JSON para no recargar la página
    @PostMapping("/confirmar/{idReserva}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> confirmarCheckin(@PathVariable("idReserva") String idReserva) {
        Optional<Reserva> resOpt = reservaRepositorio.findById(idReserva);
        if (resOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Reserva no encontrada");
        }

        Reserva r = resOpt.get();
        r.setEstado("EN_USO");
        reservaRepositorio.save(r);

        // Marcamos a todos como asistidos
        List<ReservaIntegrante> integrantes = reservaIntegranteRepositorio.findByReserva_IdReserva(idReserva);
        for (ReservaIntegrante ri : integrantes) {
            ri.setAsistio(true);
            reservaIntegranteRepositorio.save(ri);
        }

        // Marcamos los ítems como entregados
        List<ReservaItem> items = reservaItemRepositorio.findByReserva_IdReserva(idReserva);
        for (ReservaItem it : items) {
            it.setEntregado(true);
            reservaItemRepositorio.save(it);
        }

        return ResponseEntity.ok().body("{\"status\":\"ok\"}");
    }

    // ==========================================
    // DTOs auxiliares
    // ==========================================
    public static class CheckinReservaDto {

        private String idReserva;
        private String laboratorio;
        private String tipoLab;
        private String cubiculo;
        private String fecha;
        private String horario;
        private String estado;
        private List<String> integrantes;

        public CheckinReservaDto(String idReserva, String laboratorio, String tipoLab, String cubiculo, String fecha, String horario, String estado, List<String> integrantes) {
            this.idReserva = idReserva;
            this.laboratorio = laboratorio;
            this.tipoLab = tipoLab;
            this.cubiculo = cubiculo;
            this.fecha = fecha;
            this.horario = horario;
            this.estado = estado;
            this.integrantes = integrantes;
        }

        public String getIdReserva() {
            return idReserva;
        }

        public String getLaboratorio() {
            return laboratorio;
        }

        public String getTipoLab() {
            return tipoLab;
        }

        public String getCubiculo() {
            return cubiculo;
        }

        public String getFecha() {
            return fecha;
        }

        public String getHorario() {
            return horario;
        }

        public String getEstado() {
            return estado;
        }

        public List<String> getIntegrantes() {
            return integrantes;
        }
    }

    public static class DetalleModalCheckinDto {

        private String idReserva;
        private String laboratorio;
        private String horario;
        private List<IntegranteDetalleDto> integrantes;
        private List<ItemDetalleDto> insumos;
        private List<ItemDetalleDto> epps;

        public DetalleModalCheckinDto(String idReserva, String laboratorio, String horario, List<IntegranteDetalleDto> integrantes, List<ItemDetalleDto> insumos, List<ItemDetalleDto> epps) {
            this.idReserva = idReserva;
            this.laboratorio = laboratorio;
            this.horario = horario;
            this.integrantes = integrantes;
            this.insumos = insumos;
            this.epps = epps;
        }

        public String getIdReserva() {
            return idReserva;
        }

        public String getLaboratorio() {
            return laboratorio;
        }

        public String getHorario() {
            return horario;
        }

        public List<IntegranteDetalleDto> getIntegrantes() {
            return integrantes;
        }

        public List<ItemDetalleDto> getInsumos() {
            return insumos;
        }

        public List<ItemDetalleDto> getEpps() {
            return epps;
        }
    }

    public static class IntegranteDetalleDto {

        private String codigo;
        private String nombre;
        private String iniciales;

        public IntegranteDetalleDto(String codigo, String nombre, String iniciales) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.iniciales = iniciales;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getIniciales() {
            return iniciales;
        }
    }

    public static class ItemDetalleDto {

        private String id;
        private String nombre;
        private String unidad;
        private String emoji;

        public ItemDetalleDto(String id, String nombre, String unidad, String emoji) {
            this.id = id;
            this.nombre = nombre;
            this.unidad = unidad;
            this.emoji = emoji;
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getUnidad() {
            return unidad;
        }

        public String getEmoji() {
            return emoji;
        }
    }
}
