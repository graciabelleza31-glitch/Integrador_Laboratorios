package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Administrador;
import com.proyecto.integrador.modelo.Alumno;
import com.proyecto.integrador.repository.AdministradorRepository;
import com.proyecto.integrador.repository.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired 
    private AlumnoRepository alumnoRepository;

    @Autowired 
    private AdministradorRepository administradorRepository;

    /**
     * Valida las credenciales de un usuario.
     * Busca primero como alumno y luego como administrador.
     * 
     * @param codigoUtp El código UTP (ej: U15331490) o DNI del usuario
     * @param password La contraseña
     * @return Un objeto LoginResult con el tipo de usuario y los datos
     */
    public LoginResult validarCredenciales(String codigoUtp, String password) {
        
        // 1. Intentar como alumno (por idEstud)
        Optional<Alumno> alumnoOpt = alumnoRepository.findById(codigoUtp);
        if (alumnoOpt.isPresent()) {
            Alumno alumno = alumnoOpt.get();
            if (alumno.getPassword().equals(password)) {
                return new LoginResult(
                    "ALUMNO",
                    alumno.getNombre(),
                    alumno.getApellido(),
                    alumno.getIdEstud()
                );
            }
        }

        // 2. Intentar como alumno (por DNI)
        Optional<Alumno> alumnoPorDni = alumnoRepository.findByDni(codigoUtp);
        if (alumnoPorDni.isPresent()) {
            Alumno alumno = alumnoPorDni.get();
            if (alumno.getPassword().equals(password)) {
                return new LoginResult(
                    "ALUMNO",
                    alumno.getNombre(),
                    alumno.getApellido(),
                    alumno.getIdEstud()
                );
            }
        }

        // 3. Intentar como administrador (por DNI)
        Optional<Administrador> adminOpt = administradorRepository.findByDni(codigoUtp);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            if (admin.getPassword().equals(password)) {
                return new LoginResult(
                    "ADMIN",
                    admin.getNombre(),
                    admin.getApellido(),
                    admin.getIdAdmin()
                );
            }
        }

        // 4. Si nada funcionó, devolver null
        return null;
    }
    /**
     * Clase interna para devolver el resultado del login.
     */
     public static class LoginResult {
        private final String tipoUsuario; // "ALUMNO" o "ADMIN"
        private final String nombre;
        private final String apellido;
        private final String id;

        public LoginResult(String tipoUsuario, String nombre, String apellido, String id) {
            this.tipoUsuario = tipoUsuario;
            this.nombre = nombre;
            this.apellido = apellido;
            this.id = id;
        }

        public String getTipoUsuario() { return tipoUsuario; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getId() { return id; }

        public boolean esAdmin() {
            return "ADMIN".equals(tipoUsuario);
        }

        public boolean esAlumno() {
            return "ALUMNO".equals(tipoUsuario);
        }

        public String getNombreCompleto() {
            return nombre + " " + apellido;
        }
    }
}
