package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "sancion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sancion {
    @Id
    @Column(name = "id_sancion", length = 4)
    private String idSancion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @Column(length = 255)
    private String motivo;

    @Column(name = "monto_deuda")
    private Double montoDeuda;

    @Column(length = 20)
    private String estado;
}
