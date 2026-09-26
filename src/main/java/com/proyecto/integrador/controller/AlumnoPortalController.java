package com.proyecto.integrador.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import com.proyecto.integrador.modelo.Boleta;
import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.modelo.ReservaIntegrante;
import com.proyecto.integrador.modelo.ReservaItem;
import com.proyecto.integrador.modelo.Sancion;
import com.proyecto.integrador.repository.AlumnoRepository;
import com.proyecto.integrador.repository.BoletaRepository;
import com.proyecto.integrador.repository.LaboratorioRepository;
import com.proyecto.integrador.repository.ProductoRepository;
import com.proyecto.integrador.repository.ReservaIntegranteRepository;
import com.proyecto.integrador.repository.ReservaItemRepository;
import com.proyecto.integrador.repository.ReservaRepository;
import com.proyecto.integrador.repository.SancionRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/alumno")
public class AlumnoPortalController {

    @Autowired
    private LaboratorioRepository laboratorioRepositorio;

    @Autowired
    private ReservaRepository reservaRepositorio;

    @Autowired
    private ReservaIntegranteRepository reservaIntegranteRepositorio;

    @Autowired
    private ReservaItemRepository reservaItemRepositorio;

    @Autowired
    private ProductoRepository productoRepositorio;

    @Autowired
    private AlumnoRepository alumnoRepositorio;

    @Autowired
    private BoletaRepository boletaRepositorio;

    @Autowired
    private SancionRepository sancionRepositorio;

    // =========================================================================
    // 1. PANTALLA PRINCIPAL: LABORATORIOS Y GESTIÓN DE RESERVAS
    // =========================================================================
    @GetMapping("/laboratorios")
    @Transactional(readOnly = true)
    public String verLaboratorios(HttpSession sesion, Model modelo) {
        Object usuario = sesion.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Alumno alumno = null;
        if (usuario instanceof Alumno) {
            alumno = (Alumno) usuario;
        } else {
            return "redirect:/login";
        }

        // Consultar sanciones pendientes en la BD
        List<Sancion> sancionesPendientes = sancionRepositorio.findByAlumno_IdEstudAndEstado(alumno.getIdEstud(), "PENDIENTE");
        long incidenciasPendientes = sancionesPendientes.size();

        // Determinar si la cuenta está bloqueada
        String estadoCuenta = (alumno.getEstadoCuenta() != null) ? alumno.getEstadoCuenta().trim().toUpperCase() : "";
        boolean estaBloqueado = incidenciasPendientes > 0
                || "BLOQUEADO_POR_DEUDA".equals(estadoCuenta)
                || estadoCuenta.contains("BLOQUEADO");

        // Listar laboratorios
        List<Laboratorio> labs = laboratorioRepositorio.findAll();

        // Separar productos entre Insumos y EPPs
        List<Producto> todosLosProductos = productoRepositorio.findAll();
        List<Producto> insumos = new ArrayList<>();
        List<Producto> epps = new ArrayList<>();

        for (Producto p : todosLosProductos) {
            if (p.getCategoria() != null && "EPP".equalsIgnoreCase(p.getCategoria().trim())) {
                epps.add(p);
            } else {
                insumos.add(p);
            }
        }

        // Obtener reservas activas en las que participa el alumno
        List<ReservaIntegrante> participaciones = reservaIntegranteRepositorio.findByAlumno_IdEstud(alumno.getIdEstud());
        List<ReservaActivaDto> misReservas = new ArrayList<>();

        for (ReservaIntegrante ri : participaciones) {
            Reserva r = ri.getReserva();
            if (r != null && ("CONFIRMADA".equalsIgnoreCase(r.getEstado()) || "EN_USO".equalsIgnoreCase(r.getEstado()))) {
                String labNombre = (r.getLaboratorio() != null) ? r.getLaboratorio().getNombre() : "Laboratorio";
                String cubiculo = (r.getLaboratorio() != null) ? r.getLaboratorio().getUbicacionCubiculo() : "S/C";
                String fechaFormato = (r.getFechaReserva() != null) ? r.getFechaReserva().toString() : "";
                String horario = r.getHoraInicio() + "–" + r.getHoraFin();

                misReservas.add(new ReservaActivaDto(
                        r.getIdReserva(),
                        labNombre,
                        cubiculo,
                        fechaFormato,
                        horario,
                        r.getEstado()
                ));
            }
        }

        modelo.addAttribute("alumno", alumno);
        modelo.addAttribute("estaBloqueado", estaBloqueado);
        modelo.addAttribute("numIncidencias", incidenciasPendientes);
        modelo.addAttribute("laboratorios", labs);
        modelo.addAttribute("insumos", insumos);
        modelo.addAttribute("epps", epps);
        modelo.addAttribute("misReservas", misReservas);

        return "alumno-laboratorios";
    }

    // =========================================================================
    // 2. VISTA INDEPENDIENTE: CATÁLOGO DE INSUMOS
    // =========================================================================
    @GetMapping("/catalogos")
    @Transactional(readOnly = true)
    public String verCatalogo(HttpSession sesion, Model modelo) {
        Object usuario = sesion.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Alumno alumno = null;
        if (usuario instanceof Alumno) {
            alumno = (Alumno) usuario;
        } else {
            return "redirect:/login";
        }

        // Consultar incidencias para la barra lateral y campana
        List<Sancion> sancionesPendientes = sancionRepositorio.findByAlumno_IdEstudAndEstado(alumno.getIdEstud(), "PENDIENTE");
        long incidenciasPendientes = sancionesPendientes.size();

        String estadoCuenta = (alumno.getEstadoCuenta() != null) ? alumno.getEstadoCuenta().trim().toUpperCase() : "";
        boolean estaBloqueado = incidenciasPendientes > 0
                || "BLOQUEADO_POR_DEUDA".equals(estadoCuenta)
                || estadoCuenta.contains("BLOQUEADO");

        // Obtener todos los insumos de la base de datos
        List<Producto> todosLosProductos = productoRepositorio.findAll();

        // Extraer dinámicamente las categorías únicas existentes en la BD
        List<String> categoriasBD = todosLosProductos.stream()
                .map(Producto::getCategoria)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();

        modelo.addAttribute("alumno", alumno);
        modelo.addAttribute("estaBloqueado", estaBloqueado);
        modelo.addAttribute("numIncidencias", incidenciasPendientes);
        modelo.addAttribute("productos", todosLosProductos);
        modelo.addAttribute("categorias", categoriasBD);
        modelo.addAttribute("totalItems", todosLosProductos.size());

        return "alumno-catalogos";
    }

    // =========================================================================
    // 3. PROCESAR RESERVA, INTEGRANTES, ÍTEMS, STOCK Y BOLETA
    // =========================================================================
    @PostMapping("/reservas/confirmar-pago")
    @Transactional
    public String procesarPagoYReserva(
            @RequestParam("idLab") String idLab,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("horaInicio") String horaInicioStr,
            @RequestParam("horaFin") String horaFinStr,
            @RequestParam("integrantesCodigos") List<String> codigosIntegrantes,
            @RequestParam(value = "insumosIds", required = false) List<String> insumosIds,
            @RequestParam(value = "eppsIds", required = false) List<String> eppsIds,
            @RequestParam("montoTotal") BigDecimal montoTotal,
            @RequestParam(value = "metodoPago", defaultValue = "GRATUITO") String metodoPago,
            HttpSession sesion) {

        Object usuario = sesion.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        Alumno alumnoLogueado = (Alumno) usuario;

        // Doble validación de seguridad contra cuentas bloqueadas
        boolean tieneSancion = sancionRepositorio.existsByAlumno_IdEstudAndEstado(alumnoLogueado.getIdEstud(), "PENDIENTE");
        String estadoCuentaAlumno = (alumnoLogueado.getEstadoCuenta() != null) ? alumnoLogueado.getEstadoCuenta().trim().toUpperCase() : "";
        boolean cuentaBloqueada = "BLOQUEADO_POR_DEUDA".equals(estadoCuentaAlumno) || estadoCuentaAlumno.contains("BLOQUEADO");

        if (tieneSancion || cuentaBloqueada) {
            return "redirect:/alumno/laboratorios";
        }

        Optional<Laboratorio> labOpt = laboratorioRepositorio.findById(idLab);
        if (labOpt.isEmpty()) {
            return "redirect:/alumno/laboratorios";
        }
        Laboratorio lab = labOpt.get();

        LocalDate fecha = LocalDate.parse(fechaStr);
        LocalTime horaInicio = LocalTime.parse(horaInicioStr);
        LocalTime horaFin = LocalTime.parse(horaFinStr);

        // A. Generación secuencial de 6 caracteres: RES001, RES002, ..., RES006
        String idReserva = "RES001";
        Optional<String> ultimoIdOpt = reservaRepositorio.findUltimoIdReserva();

        if (ultimoIdOpt.isPresent()) {
            try {
                String ultimoId = ultimoIdOpt.get().trim();
                int num = Integer.parseInt(ultimoId.substring(3));
                idReserva = String.format("RES%03d", num + 1);
            } catch (Exception e) {
                long count = reservaRepositorio.count() + 1;
                idReserva = String.format("RES%03d", count);
            }
        }

        Reserva nuevaReserva = new Reserva(idReserva, lab, fecha, horaInicio, horaFin, "CONFIRMADA");
        reservaRepositorio.save(nuevaReserva);

        // B. Guardar Responsable (Alumno logueado)
        ReservaIntegrante responsable = new ReservaIntegrante(nuevaReserva, alumnoLogueado, false);
        reservaIntegranteRepositorio.save(responsable);

        // C. Guardar Integrantes adicionales
        if (codigosIntegrantes != null) {
            for (String cod : codigosIntegrantes) {
                if (!cod.trim().equalsIgnoreCase(alumnoLogueado.getIdEstud().trim())) {
                    alumnoRepositorio.findById(cod.trim()).ifPresent(al -> {
                        reservaIntegranteRepositorio.save(new ReservaIntegrante(nuevaReserva, al, false));
                    });
                }
            }
        }

        // D. Guardar Insumos y EPPs + Descontar Stock Físico
        List<String> productosElegidos = new ArrayList<>();
        if (insumosIds != null) {
            productosElegidos.addAll(insumosIds);
        }
        if (eppsIds != null) {
            productosElegidos.addAll(eppsIds);
        }

        for (String idProd : productosElegidos) {
            productoRepositorio.findById(idProd).ifPresent(prod -> {
                ReservaItem item = new ReservaItem(nuevaReserva, prod, 1, false);
                reservaItemRepositorio.save(item);

                // Descuento en la tabla producto
                if (prod.getStockActual() != null && prod.getStockActual() > 0) {
                    prod.setStockActual(prod.getStockActual() - 1);
                    productoRepositorio.save(prod);
                }
            });
        }

        // E. Generación correlativa de Boleta de 6 caracteres: BOL001, BOL002...
        long totalBoletas = boletaRepositorio.count() + 1;
        String idBoleta = String.format("BOL%03d", totalBoletas);

        Boleta boleta = new Boleta(
                idBoleta,
                alumnoLogueado,
                null,
                nuevaReserva,
                LocalDateTime.now(),
                montoTotal,
                "PAGO_" + metodoPago.toUpperCase()
        );
        boletaRepositorio.save(boleta);

        return "redirect:/alumno/laboratorios";
    }

    // =========================================================================
    // 4. VISTA INDEPENDIENTE: PROTOCOLOS DE SEGURIDAD
    // =========================================================================
    @GetMapping("/protocolos")
    @Transactional(readOnly = true)
    public String verProtocolos(HttpSession sesion, Model modelo) {
        Object usuario = sesion.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Alumno alumno = null;
        if (usuario instanceof Alumno) {
            alumno = (Alumno) usuario;
        } else {
            return "redirect:/login";
        }

        // Validación de sanciones y deudas para mostrar la alerta amarilla inferior
        List<Sancion> sancionesPendientes = sancionRepositorio.findByAlumno_IdEstudAndEstado(alumno.getIdEstud(), "PENDIENTE");
        long incidenciasPendientes = sancionesPendientes.size();

        String estadoCuenta = (alumno.getEstadoCuenta() != null) ? alumno.getEstadoCuenta().trim().toUpperCase() : "";
        boolean estaBloqueado = incidenciasPendientes > 0
                || "BLOQUEADO_POR_DEUDA".equals(estadoCuenta)
                || estadoCuenta.contains("BLOQUEADO");

        modelo.addAttribute("alumno", alumno);
        modelo.addAttribute("estaBloqueado", estaBloqueado);
        modelo.addAttribute("numIncidencias", incidenciasPendientes);

        return "alumno-protocolos";
    }

    // =========================================================================
    // DTO AUXILIAR PARA ENVÍO A THYMELEAF
    // =========================================================================
    public static class ReservaActivaDto {

        private String idReserva;
        private String laboratorio;
        private String cubiculo;
        private String fecha;
        private String horario;
        private String estado;

        public ReservaActivaDto(String idReserva, String laboratorio, String cubiculo, String fecha, String horario, String estado) {
            this.idReserva = idReserva;
            this.laboratorio = laboratorio;
            this.cubiculo = cubiculo;
            this.fecha = fecha;
            this.horario = horario;
            this.estado = estado;
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

        public String getFecha() {
            return fecha;
        }

        public String getHorario() {
            return horario;
        }

        public String getEstado() {
            return estado;
        }
    }
}
