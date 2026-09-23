package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "laboratorio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Laboratorio {
    @Id
    @Column(name = "id_lab", length = 6)
    private String idLab;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String tipo;

    @Column(name = "ubicacion_cubiculo", length = 50)
    private String ubicacionCubiculo;

    @OneToMany(mappedBy = "laboratorio")
    private List<Reserva> reservas;
}
