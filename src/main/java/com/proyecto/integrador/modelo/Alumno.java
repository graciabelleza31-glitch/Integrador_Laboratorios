package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "alumno")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alumno {
     @Id
    @Column(name = "id_estud", length = 9)
    private String idEstud;

    @Column(unique = true, nullable = false, length = 15)
    private String dni;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @Column(name = "es_matriculado", nullable = false)
    private Boolean esMatriculado;

    @OneToMany(mappedBy = "alumno")
    private List<ReservaIntegrante> reservasComoIntegrante;
}
