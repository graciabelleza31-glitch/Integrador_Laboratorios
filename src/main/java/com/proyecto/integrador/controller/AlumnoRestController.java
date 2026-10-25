package com.proyecto.integrador.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.repository.AlumnoRepository;
import com.proyecto.integrador.repository.LaboratorioRepository;
import com.proyecto.integrador.repository.SancionRepository;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoRestController {

    @Autowired
    private AlumnoRepository alumnoRepositorio;

    @Autowired
    private LaboratorioRepository laboratorioRepositorio;

    @Autowired
    private SancionRepository sancionRepositorio;

    @GetMapping("/buscar/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(
            @PathVariable("codigo") String codigo,
            @RequestParam(value = "idLab", required = false) String idLab) {
        
        String query = codigo.trim().toUpperCase();

        // 1. Busca por clave primaria (idEstud, ej. U24201349)
        Optional<Alumno> alumnoOpt = alumnoRepositorio.findById(query);

        // 2. Si no lo encuentra, busca por DNI
        if (alumnoOpt.isEmpty()) {
            alumnoOpt = alumnoRepositorio.findAll().stream()
                    .filter(a -> a.getDni() != null && a.getDni().trim().equalsIgnoreCase(query))
                    .findFirst();
        }

        if (alumnoOpt.isPresent()) {
            Alumno a = alumnoOpt.get();
            Map<String, Object> resp = new HashMap<>();
            resp.put("encontrado", true);
            resp.put("codigo", a.getIdEstud());
            resp.put("nombreCompleto", a.getNombre() + " " + a.getApellido());

            // A. Validar estado de cuenta y sanciones
            boolean cuentaActiva = "HABILITADO".equalsIgnoreCase(a.getEstadoCuenta()) || a.getEstadoCuenta() == null;
            long deudas = sancionRepositorio.countByAlumno_IdEstudAndEstadoNotIn(
                    a.getIdEstud(), List.of("RESUELTA", "PAGADA", "PAGADO"));
            boolean sinDeudas = (deudas == 0);

            // B. Validar permisos de su carrera
            boolean carreraPermitida = false;
            String mensajeError = "";

            if (!cuentaActiva || !sinDeudas) {
                mensajeError = "El alumno tiene sanciones pendientes o no está habilitado.";
            } else if (a.getCarrera() == null || Boolean.FALSE.equals(a.getCarrera().getPermiteLab())) {
                String nomCarrera = (a.getCarrera() != null) ? a.getCarrera().getNombre() : "Sin carrera";
                mensajeError = "La carrera del alumno (" + nomCarrera + ") no tiene permiso para usar laboratorios.";
            } else {
                List<Laboratorio> labsCarrera = laboratorioRepositorio.findLaboratoriosPermitidosPorCarrera(
                        a.getCarrera().getIdCarrera());
                
                if (labsCarrera == null || labsCarrera.isEmpty()) {
                    mensajeError = "La carrera del alumno (" + a.getCarrera().getNombre() + ") no cuenta con laboratorios asignados.";
                } else if (idLab != null && !idLab.isBlank()) {
                    // Si ya seleccionó un laboratorio en el Paso 1, verifica que el integrante también tenga permiso para ese lab
                    boolean tieneAccesoLab = labsCarrera.stream().anyMatch(l -> l.getIdLab().equalsIgnoreCase(idLab.trim()));
                    if (tieneAccesoLab) {
                        carreraPermitida = true;
                    } else {
                        mensajeError = "La carrera del alumno (" + a.getCarrera().getNombre() + ") no tiene acceso a este laboratorio.";
                    }
                } else {
                    carreraPermitida = true;
                }
            }

            boolean habilitadoFinal = cuentaActiva && sinDeudas && carreraPermitida;
            resp.put("habilitado", habilitadoFinal);
            resp.put("mensajeError", mensajeError);

            return ResponseEntity.ok(resp);
        }

        return ResponseEntity.ok(Map.of("encontrado", false));
    }
}