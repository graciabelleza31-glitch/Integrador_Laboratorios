package com.proyecto.integrador.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sancion")
public class Sancion {

    @Id
    @Column(name = "id_sancion", length = 4)
    private String idSancion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estud", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(name = "monto_deuda", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDeuda;

    @Column(length = 20)
    private String estado;

    public Sancion() {
    }

    public Sancion(String idSancion, Reserva reserva, Alumno alumno, Producto producto, String motivo, BigDecimal montoDeuda, String estado) {
        this.idSancion = idSancion;
        this.reserva = reserva;
        this.alumno = alumno;
        this.producto = producto;
        this.motivo = motivo;
        this.montoDeuda = montoDeuda;
        this.estado = estado;
    }

    public String getIdSancion() {
        return idSancion;
    }

    public void setIdSancion(String idSancion) {
        this.idSancion = idSancion;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getMontoDeuda() {
        return montoDeuda;
    }

    public void setMontoDeuda(BigDecimal montoDeuda) {
        this.montoDeuda = montoDeuda;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
