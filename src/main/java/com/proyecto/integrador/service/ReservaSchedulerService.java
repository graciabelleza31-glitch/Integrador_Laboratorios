package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Reserva; // Ajusta si tu entidad se llama distinto
import com.proyecto.integrador.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaSchedulerService {

    @Autowired
    private ReservaRepository reservaRepository;

    // Se ejecuta automáticamente cada 60 segundos (1 minuto)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirarReservasAntiguas() {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Buscamos las reservas que dicen "EN USO" y su hora de fin ya pasó
        // ⚠️ Ajusta "EN USO" por el texto exacto que uses en tu BD (ej. "En Uso", "ACTIVA")
        List<Reserva> reservasExpiradas = reservaRepository
                .findByEstadoAndFechaHoraFinBefore("EN USO", ahora);

        if (!reservasExpiradas.isEmpty()) {
            System.out.println("⏰ [SCHEDULER] Encontradas " + reservasExpiradas.size() + " reservas expiradas. Actualizando...");
            
            for (Reserva reserva : reservasExpiradas) {
                // ⚠️ Ajusta "DEVUELTA" por el texto exacto que uses para cuando se termina la reserva
                reserva.setEstado("DEVUELTA"); 
                reservaRepository.save(reserva);
            }
            
            System.out.println("✅ [SCHEDULER] Reservas actualizadas correctamente.");
        }
    }
}
