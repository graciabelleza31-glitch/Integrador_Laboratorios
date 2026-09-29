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
public class TareaProgramadaService {

    @Autowired
    private ReservaRepository reservaRepository;

    // Tiempo de tolerancia en minutos después de la hora de inicio
    private static final int MINUTOS_TOLERANCIA = 10;

    /**
     * Se ejecuta cada 1 minuto (para pruebas). En producción usar 300000 (5 min).
     * 
     * Lógica:
     * - Cancela reservas CONFIRMADAS que no fueron atendidas (pasaron X min de tolerancia).
     * - NO toca reservas EN_USO (espera a que el admin haga check-out manual).
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    @Transactional
    public void cancelarReservasNoAsistidas() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        // Solo buscar reservas CONFIRMADAS (no EN_USO, no DEVUELTA, etc.)
        List<Reserva> reservasConfirmadas = reservaRepository.findByEstado("CONFIRMADA");

        System.out.println("⏰ [JOB] " + LocalDateTime.now() + 
                " - Reservas CONFIRMADAS encontradas: " + reservasConfirmadas.size());

        int canceladas = 0;
        for (Reserva r : reservasConfirmadas) {
            if (r.getHoraInicio() == null) continue;

            boolean noAsistio = false;

            // Caso 1: Es de HOY y pasaron más de 10 minutos desde hora_inicio
            if (r.getFechaReserva().equals(hoy)) {
                LocalTime limiteTolerancia = r.getHoraInicio().plusMinutes(MINUTOS_TOLERANCIA);
                if (ahora.isAfter(limiteTolerancia)) {
                    noAsistio = true;
                }
            }
            // Caso 2: Es de un día ANTERIOR (nunca hizo check-in)
            else if (r.getFechaReserva().isBefore(hoy)) {
                noAsistio = true;
            }

            if (noAsistio) {
                r.setEstado("NO_ASISTIO");  // O "CANCELADA" si prefieres no agregar el estado nuevo
                reservaRepository.save(r);
                canceladas++;
                System.out.println("   ❌ " + r.getIdReserva() + 
                        " cancelada por NO ASISTIR (fecha: " + r.getFechaReserva() + 
                        ", hora_inicio: " + r.getHoraInicio() + ")");
            }
        }

        if (canceladas > 0) {
            System.out.println("[" + LocalDateTime.now() + "] Total canceladas: " + canceladas);
        }
    }
}