package com.proyecto.integrador.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // NUEVO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // NUEVO

import com.proyecto.integrador.dto.CambioPasswordDTO; // NUEVO (asegúrate de tenerlo creado)
import com.proyecto.integrador.exception.CambioPasswordException; // NUEVO (asegúrate de tenerlo creado)
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

    @Autowired // NUEVO: Inyectamos el encriptador
    private PasswordEncoder passwordEncoder;

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

            // MODIFICADO: Ahora usamos passwordEncoder.matches() en vez de .equals()
            if (!passwordEncoder.matches(clave, alumno.getPassword())) {
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

            // MODIFICADO: Ahora usamos passwordEncoder.matches() en vez de .equals()
            if (!passwordEncoder.matches(clave, admin.getPassword())) {
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

        // NUEVO MÉTODO: Para restablecer contraseña desde el Login (Olvidé mi contraseña)
    @Transactional
    public void restablecerPasswordAlumno(String idAlumno, String nuevaPassword, String confirmacionPassword) {
        // 1. Buscar al alumno
        Alumno alumno = alumnoRepositorio.findById(idAlumno)
                .orElseThrow(() -> new CambioPasswordException("Alumno no encontrado"));

        // 2. Validar que la nueva y la confirmación coincidan
        if (!nuevaPassword.equals(confirmacionPassword)) {
            throw new CambioPasswordException("La nueva contraseña y su confirmación no coinciden.");
        }

        // 3. VALIDACIÓN CLAVE: que la nueva NO sea igual a la actual
        if (passwordEncoder.matches(nuevaPassword, alumno.getPassword())) {
            throw new CambioPasswordException("La nueva contraseña no puede ser igual a la anterior.");
        }

        // 4. Validar longitud mínima
        if (nuevaPassword.length() < 8) {
            throw new CambioPasswordException("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        // 5. Encriptar y guardar
        alumno.setPassword(passwordEncoder.encode(nuevaPassword));
        alumnoRepositorio.save(alumno);
    }

    // ==============================================================
    // NUEVO MÉTODO: Para cambiar la contraseña del Alumno
    // ==============================================================
    @Transactional
    public void cambiarPasswordAlumno(String idAlumno, CambioPasswordDTO dto) {
        Alumno alumno = alumnoRepositorio.findById(idAlumno)
                .orElseThrow(() -> new CambioPasswordException("Alumno no encontrado"));

        // ⚠️ TEMPORAL: Validamos en texto plano (solo para tu prueba)
        if (!dto.getPasswordActual().equals(alumno.getPassword())) {
            throw new CambioPasswordException("La contraseña actual es incorrecta.");
        }

        if (!dto.getPasswordNueva().equals(dto.getPasswordConfirmacion())) {
            throw new CambioPasswordException("La nueva contraseña y su confirmación no coinciden.");
        }

        // ⚠️ TEMPORAL: Validación clave en texto plano
        if (dto.getPasswordNueva().equals(alumno.getPassword())) {
            throw new CambioPasswordException("La nueva contraseña no puede ser igual a la anterior.");
        }

        // ⚠️ TEMPORAL: Guardamos en texto plano
        alumno.setPassword(dto.getPasswordNueva());
        alumnoRepositorio.save(alumno);
    }


    // ==============================================================
    // MÉTODOS EXISTENTES
    // ==============================================================
    public static class ResultadoLogin {
        // ... (deja tu clase ResultadoLogin exactamente igual que como la tienes)
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

        public String getRol() { return rol; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getEstadoCuenta() { return estadoCuenta; }
        public String getCarrera() { return carrera; }
        public String getNombreCompleto() { return nombre + " " + apellido; }
        public boolean esAdmin() { return "ADMIN".equalsIgnoreCase(rol); }
        public boolean esAlumno() { return "ALUMNO".equalsIgnoreCase(rol); }
    }
}