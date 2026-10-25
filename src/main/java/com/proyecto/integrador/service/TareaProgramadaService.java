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
import java.time.ZoneId;
import java.util.List;

@Service
public class TareaProgramadaService {

    @Autowired
    private ReservaRepository reservaRepository;

    // 🔑 Zona horaria de Perú (UTC-5)
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    // Tiempo de tolerancia en minutos después de la hora de inicio
    private static final int MINUTOS_TOLERANCIA = 10;

    /**
     * Se ejecuta cada 1 minuto (para pruebas). En producción usar 300000 (5 min).
     * 
     * Lógica:
     * - Cancela reservas CONFIRMADAS que no fueron atendidas (pasaron X min de tolerancia).
     * - NO toca reservas EN_USO (espera a que el admin haga check-out manual).
     * 
     * ⚠️ IMPORTANTE: Se usa la zona horaria de Perú (America/Lima),
     * no la del servidor (que está en UTC).
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    @Transactional
    public void cancelarReservasNoAsistidas() {
        // 🔑 Usar zona horaria de Perú en lugar de UTC
        LocalDate hoy = LocalDate.now(ZONA_PERU);
        LocalTime ahora = LocalTime.now(ZONA_PERU);
        LocalDateTime ahoraCompleto = LocalDateTime.now(ZONA_PERU);

        // Solo buscar reservas CONFIRMADAS
        List<Reserva> reservasConfirmadas = reservaRepository.findByEstado("CONFIRMADA");

        System.out.println("⏰ [JOB] " + ahoraCompleto + 
                " (Perú: " + ahora + ")" +
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
                r.setEstado("NO_ASISTIO");
                reservaRepository.save(r);
                canceladas++;
                System.out.println("   ❌ " + r.getIdReserva() + 
                        " cancelada por NO ASISTIR (fecha: " + r.getFechaReserva() + 
                        ", hora_inicio: " + r.getHoraInicio() + ")");
            }
        }

        if (canceladas > 0) {
            System.out.println("[" + LocalDateTime.now(ZONA_PERU) + "] Total canceladas: " + canceladas);
        }
    }
}