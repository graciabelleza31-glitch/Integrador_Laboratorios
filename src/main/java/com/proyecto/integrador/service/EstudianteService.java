package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.*;
import com.proyecto.integrador.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EstudianteService {
    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ReservaIntegranteRepository reservaIntegranteRepository;

    @Autowired
    private SancionRepository sancionRepository;

    public Optional<Alumno> obtenerEstudiante(String idEstud) {
        return alumnoRepository.findById(idEstud);
    }

    public List<Reserva> obtenerReservasActivas(String idEstud) {
        List<ReservaIntegrante> integrantes = reservaIntegranteRepository.findByAlumnoIdEstud(idEstud);
        List<Reserva> reservasActivas = new ArrayList<>();

        for (ReservaIntegrante ri : integrantes) {
            Reserva reserva = ri.getReserva();
            if ("PENDIENTE".equals(reserva.getEstado()) ||
                "CONFIRMADA".equals(reserva.getEstado()) ||
                "EN_USO".equals(reserva.getEstado())) {
                reservasActivas.add(reserva);
            }
        }
        return reservasActivas;
    }

    public List<Reserva> obtenerHistorialReservas(String idEstud) {
        List<ReservaIntegrante> integrantes = reservaIntegranteRepository.findByAlumnoIdEstud(idEstud);
        List<Reserva> reservas = new ArrayList<>();
        for (ReservaIntegrante ri : integrantes) {
            reservas.add(ri.getReserva());
        }
        return reservas;
    }

    public boolean tieneSancionesPendientes(String idEstud) {
        List<Sancion> pendientes = obtenerSancionesPendientes(idEstud);
        return !pendientes.isEmpty();
    }

    public List<Sancion> obtenerSancionesPendientes(String idEstud) {
        List<ReservaIntegrante> integrantes = reservaIntegranteRepository.findByAlumnoIdEstud(idEstud);
        List<Sancion> pendientes = new ArrayList<>();

        for (ReservaIntegrante ri : integrantes) {
            List<Sancion> sanciones = sancionRepository.findByReservaIdReserva(ri.getReserva().getIdReserva());
            for (Sancion s : sanciones) {
                if ("PENDIENTE".equals(s.getEstado())) {
                    pendientes.add(s);
                }
            }
        }
        return pendientes;
    }
}
