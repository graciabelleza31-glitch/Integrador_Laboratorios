package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @Column(name = "id_reserva", length = 6)
    private String idReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lab", nullable = false)
    private Laboratorio laboratorio;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(length = 20)
    private String estado;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ReservaItem> items;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ReservaIntegrante> integrantes;
}
