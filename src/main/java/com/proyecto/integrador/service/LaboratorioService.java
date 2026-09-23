package com.proyecto.integrador.service;

import com.proyecto.integrador.modelo.Laboratorio;
import com.proyecto.integrador.repository.LaboratorioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LaboratorioService {
    @Autowired
    private LaboratorioRepository laboratorioRepository;

    /**
     * Obtiene todos los laboratorios.
     */
    public List<Laboratorio> listarTodos() {
        return laboratorioRepository.findAll();
    }

    /**
     * Busca un laboratorio por su ID.
     */
    public Optional<Laboratorio> buscarPorId(String idLab) {
        return laboratorioRepository.findById(idLab);
    }

    /**
     * Busca laboratorios por tipo (Química, Física, Mecatrónica).
     */
    public List<Laboratorio> buscarPorTipo(String tipo) {
        return laboratorioRepository.findByTipo(tipo);
    }

    /**
     * Guarda o actualiza un laboratorio.
     */
    public Laboratorio guardar(Laboratorio laboratorio) {
        return laboratorioRepository.save(laboratorio);
    }

    /**
     * Elimina un laboratorio por su ID.
     */
    public void eliminar(String idLab) {
        laboratorioRepository.deleteById(idLab);
    }

    /**
     * Cuenta el total de laboratorios.
     */
    public long contarTotal() {
        return laboratorioRepository.count();
    }
}
