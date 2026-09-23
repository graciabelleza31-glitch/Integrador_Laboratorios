package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "carrera")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrera {
    @Id
    @Column(name="id_carrera", length=5)
    private String idcarrera;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "permite_lab", nullable = false)
    private Boolean permiteLab;

    @OneToMany(mappedBy = "carrera")
    private List<Alumno> alumnos;
}
