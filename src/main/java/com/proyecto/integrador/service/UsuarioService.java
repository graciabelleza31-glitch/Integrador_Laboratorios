package com.proyecto.integrador.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.integrador.modelo.Administrador;
import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.repository.AdministradorRepository;
import com.proyecto.integrador.repository.AlumnoRepository;

@Service
public class UsuarioService {

    @Autowired
    private AlumnoRepository alumnoRepositorio;

    @Autowired
    private AdministradorRepository administradorRepositorio;

    public ResultadoLogin validarCredenciales(String identificador, String password) {
        if (identificador == null || identificador.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Por favor, ingresa tu usuario y contraseña.");
        }

        String entrada = identificador.trim();
        String clave = password.trim();

        Optional<Alumno> alumnoOpcional = alumnoRepositorio.findById(entrada.toUpperCase());
        if (alumnoOpcional.isEmpty()) {
            alumnoOpcional = alumnoRepositorio.findByDni(entrada);
        }

        if (alumnoOpcional.isPresent()) {
            Alumno alumno = alumnoOpcional.get();

            if (!alumno.getPassword().equals(clave)) {
                throw new RuntimeException("Código UTP o contraseña incorrectos.");
            }

            if (Boolean.FALSE.equals(alumno.getEsMatriculado())) {
                throw new RuntimeException("El estudiante no figura como matriculado en el ciclo actual.");
            }

            String nombreCarrera = (alumno.getCarrera() != null)
                    ? alumno.getCarrera().getNombre()
                    : "General";

            return new ResultadoLogin(
                    "ALUMNO",
                    alumno.getIdEstud(),
                    alumno.getNombre(),
                    alumno.getApellido(),
                    alumno.getEstadoCuenta(),
                    nombreCarrera
            );
        }

        Optional<Administrador> adminOpcional = administradorRepositorio.findById(entrada.toUpperCase());
        if (adminOpcional.isEmpty()) {
            adminOpcional = administradorRepositorio.findByDni(entrada);
        }

        if (adminOpcional.isPresent()) {
            Administrador admin = adminOpcional.get();

            if (!admin.getPassword().equals(clave)) {
                throw new RuntimeException("Código o contraseña incorrectos.");
            }

            return new ResultadoLogin(
                    "ADMIN",
                    admin.getIdAdmin(),
                    admin.getNombre(),
                    admin.getApellido(),
                    "HABILITADO",
                    "Gestión de Laboratorios"
            );
        }

        throw new RuntimeException("Código UTP o usuario no encontrado en el sistema.");
    }

    public static class ResultadoLogin {

        private final String rol;
        private final String codigo;
        private final String nombre;
        private final String apellido;
        private final String estadoCuenta;
        private final String carrera;

        public ResultadoLogin(String rol, String codigo, String nombre, String apellido, String estadoCuenta, String carrera) {
            this.rol = rol;
            this.codigo = codigo;
            this.nombre = nombre;
            this.apellido = apellido;
            this.estadoCuenta = estadoCuenta;
            this.carrera = carrera;
        }

        public String getRol() {
            return rol;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getApellido() {
            return apellido;
        }

        public String getEstadoCuenta() {
            return estadoCuenta;
        }

        public String getCarrera() {
            return carrera;
        }

        public String getNombreCompleto() {
            return nombre + " " + apellido;
        }

        public boolean esAdmin() {
            return "ADMIN".equalsIgnoreCase(rol);
        }

        public boolean esAlumno() {
            return "ALUMNO".equalsIgnoreCase(rol);
        }
    }
}
