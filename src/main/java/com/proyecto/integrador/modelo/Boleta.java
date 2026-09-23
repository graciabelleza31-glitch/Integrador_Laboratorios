package com.proyecto.integrador.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "boleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {
    @Id
    @Column(name = "id_boleta", length = 6)
    private String idBoleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estud")
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin")
    private Administrador administrador;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private Double total;

    @Column(name = "tipo_operacion", length = 50)
    private String tipoOperacion;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL)
    private List<DetalleBoleta> detalles;
}
