package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspeccionSchedulerService {

    @Autowired
    private ReservaRepository reservaRepository;

    // Se ejecuta cada minuto
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void vencerInspecciones() {
        LocalDate hoy = LocalDate.now();

        // Buscamos reservas que estén DEVUELTAS o EN_USO (por si no hicieron checkout)
        List<Reserva> reservasPendientes = reservaRepository.findByEstadoAndFechaReservaLessThanEqual("DEVUELTA", hoy);
        
        for (Reserva r : reservasPendientes) {
            // Calculamos la hora exacta de fin sumando 30 minutos
            LocalDateTime horaFin = LocalDateTime.of(r.getFechaReserva(), r.getHoraFin());
            LocalDateTime limite = horaFin.plusMinutes(30);

            // Si la hora actual superó el límite, se marca como NO_INSPECCIONADO
            if (LocalDateTime.now().isAfter(limite)) {
                r.setEstado("NO_INSPECCIONADO");
                reservaRepository.save(r);
                System.out.println("⚠️ [SCHEDULER] Reserva " + r.getIdReserva() + " pasó a NO_INSPECCIONADO.");
            }
        }
    }
}