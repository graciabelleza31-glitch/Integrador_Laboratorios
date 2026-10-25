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
        
        // Buscamos con el guion bajo tal cual está en tu BD
        List<Reserva> candidatas = reservaRepository.findByEstadoAndFechaReservaLessThanEqual("EN_USO", hoy);

        int contador = 0;
        for (Reserva reserva : candidatas) {
            boolean esDeDiasPasados = reserva.getFechaReserva().isBefore(hoy);
            boolean esDeHoyPeroYaTermino = reserva.getFechaReserva().isEqual(hoy) && reserva.getHoraFin().isBefore(horaActual);

            if (esDeDiasPasados || esDeHoyPeroYaTermino) {
                reserva.setEstado("DEVUELTA"); // O "FINALIZADA", como uses
                reservaRepository.save(reserva);
                contador++;
            }
        }

        if (contador > 0) {
            System.out.println("⏰ [SCHEDULER] Se cerraron " + contador + " reservas.");
        }
    }
}