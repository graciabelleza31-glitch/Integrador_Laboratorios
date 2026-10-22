package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class InspeccionSchedulerService {

    @Autowired
    private ReservaRepository reservaRepository;

    // Se ejecuta cada minuto
    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void verificarInspeccionesVencidas() {
        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // 1. Buscamos las reservas que ya se DEVOLVIERON (o están EN USO) pero que ya pasó su hora
        List<Reserva> reservasPendientes = reservaRepository.findByEstadoAndFechaReservaLessThanEqual("DEVUELTA", hoy);
        
        // (También podrías incluir "EN_USO" si el alumno no hizo check-out)

        for (Reserva reserva : reservasPendientes) {
            // Calculamos la fecha y hora exacta en la que terminó la reserva
            LocalDateTime horaFinReserva = LocalDateTime.of(reserva.getFechaReserva(), reserva.getHoraFin());
            
            // Le sumamos 30 minutos de tolerancia
            LocalDateTime limiteTolerancia = horaFinReserva.plusMinutes(30);

            // Si la hora actual ya superó el límite (pasaron más de 30 min), se cancela la inspección
            if (LocalDateTime.now().isAfter(limiteTolerancia)) {
                reserva.setEstado("NO_INSPECCIONADO"); 
                reservaRepository.save(reserva);
                
                System.out.println("⚠️ [INSPECCIÓN] La reserva " + reserva.getIdReserva() + " pasó a estado NO_INSPECCIONADO por falta de atención.");
                
                // OPCIONAL: Aquí podrías generar una incidencia automática al alumno
                // o enviar un correo al administrador para que sepa que se le pasó.
            }
        }
    }
}
