package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.PsicologoRequestDTO;
import br.unitins.tp2.dto.PsicologoResponseDTO;
import br.unitins.tp2.model.Psicologo;
import jakarta.validation.Valid;

public interface PsicologoService {

    Psicologo create(@Valid PsicologoRequestDTO psicologo);
    void update(long id, PsicologoRequestDTO psicologo);
    void delete(long id);
    Psicologo findById(long id);
    PsicologoResponseDTO findByCpf(String cpf);
    Psicologo findByCrp(String crp);
    List<Psicologo> findAll(int page, int pageSize);
    List<Psicologo> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}