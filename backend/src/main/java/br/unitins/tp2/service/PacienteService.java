package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.PacienteRequestDTO;
import br.unitins.tp2.dto.PacienteResponseDTO;
import br.unitins.tp2.model.Paciente;
import jakarta.validation.Valid;

public interface PacienteService {

    Paciente create(@Valid PacienteRequestDTO paciente);
    void update(long id, PacienteRequestDTO paciente);
    void delete(long id);
    Paciente findById(long id);
    PacienteResponseDTO findByCpf(String cpf);
    List<Paciente> findAll(int page, int pageSize);
    List<Paciente> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}