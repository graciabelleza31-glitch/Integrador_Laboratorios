package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Reserva;
import com.proyecto.integrador.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaSchedulerService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Scheduled(fixedRate = 60000) // Cada 1 minuto
    @Transactional
    public void expirarReservasAntiguas() {
        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();
        
        // 1. Buscamos todas las reservas "EN USO" de hoy o días pasados
        // ⚠️ Cambia "EN USO" por el estado exacto que tengas en tu BD (ej. "En Uso", "ACTIVA")
        List<Reserva> candidatas = reservaRepository.findByEstadoAndFechaReservaLessThanEqual("EN_USO", hoy);

        int contador = 0;
        for (Reserva reserva : candidatas) {
            // 2. Verificamos si la hora de fin ya pasó
            boolean esDeDiasPasados = reserva.getFechaReserva().isBefore(hoy);
            boolean esDeHoyPeroYaTermino = reserva.getFechaReserva().isEqual(hoy) && reserva.getHoraFin().isBefore(horaActual);

            if (esDeDiasPasados || esDeHoyPeroYaTermino) {
                // 3. Si ya terminó, la cerramos
                reserva.setEstado("DEVUELTA"); // ⚠️ Cambia "DEVUELTA" por el estado final que uses
                reservaRepository.save(reserva);
                contador++;
            }
        }

        if (contador > 0) {
            System.out.println("⏰ [SCHEDULER] Se cerraron " + contador + " reservas que ya habían expirado.");
        }
    }
}