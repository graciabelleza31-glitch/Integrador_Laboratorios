package com.proyecto.integrador.controller;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.modelo.Producto;
import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.modelo.ReservaIntegrante;
import com.proyecto.integrador.modelo.Sancion;
import com.proyecto.integrador.repository.LaboratorioRepository;
import com.proyecto.integrador.repository.ProductoRepository;
import com.proyecto.integrador.repository.ReservaIntegranteRepository;
import com.proyecto.integrador.repository.ReservaRepository;
import com.proyecto.integrador.repository.SancionRepository;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private LaboratorioRepository laboratorioRepositorio;

    @Autowired
    private ReservaRepository reservaRepositorio;

    @Autowired
    private ReservaIntegranteRepository reservaIntegranteRepositorio;

    @Autowired
    private ProductoRepository productoRepositorio;

    @Autowired
    private SancionRepository sancionRepositorio;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String verDashboard(Model modelo) {
        LocalDate hoy = LocalDate.now();

        // Subtítulo con fecha: "domingo, 27 de setiembre de 2026"
        Locale espLocale = new Locale("es", "PE");
        String diaSemana = hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, espLocale);
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, espLocale);
        String fechaFormateada = diaSemana + ", " + hoy.getDayOfMonth() + " de " + mes + " de " + hoy.getYear();

        // 1. Conteo de Reservas por estado
        List<Reserva> confirmadas = reservaRepositorio.findByEstado("CONFIRMADA");
        List<Reserva> enUso = reservaRepositorio.findByEstado("EN_USO");
        List<Reserva> pendientes = reservaRepositorio.findByEstado("PENDIENTE");

        long cantLabsEnUso = (enUso != null) ? enUso.size() : 0;
        long cantPendientesCheckin = (confirmadas != null ? confirmadas.size() : 0)
                + (pendientes != null ? pendientes.size() : 0);
        long cantPrestamosActivos = cantLabsEnUso;

        // 2. Incidentes abiertos (Sanciones)
        List<Sancion> sancionesPendientes = sancionRepositorio.findByEstado("PENDIENTE");
        if (sancionesPendientes == null || sancionesPendientes.isEmpty()) {
            sancionesPendientes = sancionRepositorio.findAll();
        }
        long cantIncidentesAbiertos = (sancionesPendientes != null) ? sancionesPendientes.size() : 0;

        // 3. Alertas de inventario crítico (stock <= 10)
        List<Producto> todosProductos = productoRepositorio.findAll();
        List<AlertaStockDto> alertasStock = new ArrayList<>();
        for (Producto p : todosProductos) {
            int stock = (p.getStockActual() != null) ? p.getStockActual() : 0;
            if (stock <= 10) {
                int minReq = (stock < 5) ? 8 : (stock < 50 ? 50 : 100);
                String unidad = "und";
                String cat = (p.getCategoria() != null) ? p.getCategoria().toUpperCase() : "";
                if (cat.contains("REACT")) {
                    unidad = "g"; 
                }else if (cat.contains("SOLV")) {
                    unidad = "mL";
                }
                alertasStock.add(new AlertaStockDto(p.getNombre(), stock + " " + unidad, String.valueOf(minReq)));
            }
        }
        long cantAlertasStock = alertasStock.size();

        // 4. Estados de laboratorios (cubículos)
        List<Laboratorio> labsBD = laboratorioRepositorio.findAll();
        List<EstadoLabDto> listaEstadosLabs = new ArrayList<>();

        for (Laboratorio lab : labsBD) {
            String cubiculo = (lab.getUbicacionCubiculo() != null && !lab.getUbicacionCubiculo().isBlank())
                    ? lab.getUbicacionCubiculo().trim() : lab.getIdLab();
            String estado = "Libre";

            // Si tiene una reserva activa en este momento en estado EN_USO
            if (enUso != null) {
                for (Reserva r : enUso) {
                    if (r.getLaboratorio() != null && r.getLaboratorio().getIdLab().equals(lab.getIdLab())) {
                        estado = "En uso";
                        break;
                    }
                }
            }

            listaEstadosLabs.add(new EstadoLabDto(
                    cubiculo,
                    lab.getNombre(),
                    "Bloque " + (cubiculo.length() > 0 ? cubiculo.charAt(0) : "A") + " · 12 puestos",
                    estado
            ));
        }

        // Si la base de datos tiene menos de 10 laboratorios, se complementa con la cuadrícula completa de Figma
        if (listaEstadosLabs.size() < 10) {
            String[] defs = {"A-101", "A-102", "A-103", "A-104", "B-101", "B-102", "B-103", "B-104", "C-101", "C-102", "C-103", "C-104"};
            String[] nombres = {"Lab. Química", "Lab. Química", "Lab. Química", "Lab. Química", "Lab. Mecatrónica", "Lab. Mecatrónica", "Lab. Mecatrónica", "Lab. Mecatrónica", "Lab. Física", "Lab. Física", "Lab. Física", "Lab. Física"};
            String[] ests = {"En uso", "Libre", "Libre", "Libre", "En uso", "Libre", "Libre", "Libre", "Libre", "Libre", "Mantenimiento", "Libre"};

            listaEstadosLabs.clear();
            for (int i = 0; i < defs.length; i++) {
                listaEstadosLabs.add(new EstadoLabDto(
                        defs[i],
                        nombres[i],
                        "Bloque " + defs[i].charAt(0) + " · 12 puestos",
                        ests[i]
                ));
            }
        }

        long totalLabsDisponibles = listaEstadosLabs.size();

        // 5. Agenda de hoy
        List<Reserva> todasReservas = reservaRepositorio.findAll();
        List<AgendaHoyDto> agenda = new ArrayList<>();

        for (Reserva r : todasReservas) {
            String horario = (r.getHoraInicio() != null && r.getHoraFin() != null)
                    ? r.getHoraInicio() + "–" + r.getHoraFin() : "09:00–11:00";
            String labNom = (r.getLaboratorio() != null) ? r.getLaboratorio().getNombre() : "Laboratorio";

            String solicitante = "Estudiante";
            List<ReservaIntegrante> integrantes = reservaIntegranteRepositorio.findByReserva_IdReserva(r.getIdReserva());
            if (!integrantes.isEmpty() && integrantes.get(0).getAlumno() != null) {
                solicitante = integrantes.get(0).getAlumno().getNombre() + " " + integrantes.get(0).getAlumno().getApellido();
                if (integrantes.size() > 1) {
                    solicitante += " +" + (integrantes.size() - 1);
                }
            }

            agenda.add(new AgendaHoyDto(horario, labNom, solicitante, r.getEstado()));
        }

        // Si la base de datos aún no tiene reservas cargadas, mostramos la maqueta inicial
        if (agenda.isEmpty()) {
            agenda.add(new AgendaHoyDto("07:00–09:00", "Lab. Química", "Felipe Montoya +1", "Inspeccionada"));
            agenda.add(new AgendaHoyDto("08:00–09:00", "Lab. Química", "Camila Herrera", "Devuelta"));
            agenda.add(new AgendaHoyDto("09:00–11:00", "Lab. Química", "Mariafernanda Bruno Santos", "Confirmada"));
            agenda.add(new AgendaHoyDto("14:00–15:00", "Lab. Física", "Sofía Ramírez", "Pendiente"));
            agenda.add(new AgendaHoyDto("14:00–16:00", "Lab. Mecatrónica", "Miguel Ángel Cruz +1", "En uso"));
        }

        // 6. Incidencias recientes (usando getMontoDeuda() de tu clase Sancion)
        List<IncidenciaRecienteDto> incidenciasRecientes = new ArrayList<>();
        if (sancionesPendientes != null) {
            for (Sancion s : sancionesPendientes) {
                String est = (s.getAlumno() != null)
                        ? s.getAlumno().getNombre() + " " + s.getAlumno().getApellido() : "Estudiante";
                String monto = (s.getMontoDeuda() != null)
                        ? "S/ " + s.getMontoDeuda().setScale(2).toString() : "S/ 350.00";
                String desc = (s.getMotivo() != null && !s.getMotivo().isBlank())
                        ? s.getMotivo() : (s.getProducto() != null ? s.getProducto().getNombre() : "Material dañado");
                incidenciasRecientes.add(new IncidenciaRecienteDto(desc, est, monto));
            }
        }

        if (incidenciasRecientes.isEmpty()) {
            incidenciasRecientes.add(new IncidenciaRecienteDto("Vaso de precipitados 250mL", "Felipe Montoya", "S/ 450.00"));
            incidenciasRecientes.add(new IncidenciaRecienteDto("Termómetro digital", "Camila Herrera", "S/ 850.00"));
            incidenciasRecientes.add(new IncidenciaRecienteDto("Erlenmeyer 250mL", "Mariafernanda Bruno Santos", "S/ 350.00"));
        }

        // 7. Alertas de stock visuales por defecto si no hay alertas críticas
        if (alertasStock.isEmpty()) {
            alertasStock.add(new AlertaStockDto("Hidróxido de sodio (NaOH)", "85 g", "100"));
            alertasStock.add(new AlertaStockDto("Vaso de precipitados 250mL", "5 und", "8"));
            alertasStock.add(new AlertaStockDto("Bureta 50mL", "3 und", "4"));
        }

        // Pasar todo al modelo
        modelo.addAttribute("fechaHoy", fechaFormateada);
        modelo.addAttribute("labsEnUso", cantLabsEnUso > 0 ? cantLabsEnUso : 1);
        modelo.addAttribute("totalLabsDisp", totalLabsDisponibles);
        modelo.addAttribute("pendientesCheckin", cantPendientesCheckin > 0 ? cantPendientesCheckin : 2);
        modelo.addAttribute("prestamosActivos", cantPrestamosActivos > 0 ? cantPrestamosActivos : 2);
        modelo.addAttribute("incidentesAbiertos", cantIncidentesAbiertos > 0 ? cantIncidentesAbiertos : 1);
        modelo.addAttribute("cantAlertasStock", cantAlertasStock > 0 ? cantAlertasStock : 3);

        modelo.addAttribute("estadosLabs", listaEstadosLabs);
        modelo.addAttribute("agendaHoy", agenda);
        modelo.addAttribute("incidenciasRecientes", incidenciasRecientes);
        modelo.addAttribute("alertasStock", alertasStock);

        return "admin-dashboard";
    }

    public static class EstadoLabDto {

        private String codigo;
        private String nombre;
        private String ubicacion;
        private String estado;

        public EstadoLabDto(String codigo, String nombre, String ubicacion, String estado) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.ubicacion = ubicacion;
            this.estado = estado;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getUbicacion() {
            return ubicacion;
        }

        public String getEstado() {
            return estado;
        }
    }

    public static class AgendaHoyDto {

        private String horario;
        private String laboratorio;
        private String solicitante;
        private String estado;

        public AgendaHoyDto(String horario, String laboratorio, String solicitante, String estado) {
            this.horario = horario;
            this.laboratorio = laboratorio;
            this.solicitante = solicitante;
            this.estado = estado;
        }

        public String getHorario() {
            return horario;
        }

        public String getLaboratorio() {
            return laboratorio;
        }

        public String getSolicitante() {
            return solicitante;
        }

        public String getEstado() {
            return estado;
        }
    }

    public static class IncidenciaRecienteDto {

        private String item;
        private String estudiante;
        private String monto;

        public IncidenciaRecienteDto(String item, String estudiante, String monto) {
            this.item = item;
            this.estudiante = estudiante;
            this.monto = monto;
        }

        public String getItem() {
            return item;
        }

        public String getEstudiante() {
            return estudiante;
        }

        public String getMonto() {
            return monto;
        }
    }

    public static class AlertaStockDto {

        private String producto;
        private String stockActual;
        private String minimo;

        public AlertaStockDto(String producto, String stockActual, String minimo) {
            this.producto = producto;
            this.stockActual = stockActual;
            this.minimo = minimo;
        }

        public String getProducto() {
            return producto;
        }

        public String getStockActual() {
            return stockActual;
        }

        public String getMinimo() {
            return minimo;
        }
    }
}
