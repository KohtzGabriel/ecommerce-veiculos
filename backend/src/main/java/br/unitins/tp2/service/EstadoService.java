package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.EstadoRequestDTO;
import br.unitins.tp2.model.Estado;
import jakarta.validation.Valid;

public interface EstadoService {

    Estado create(@Valid EstadoRequestDTO estado);
    void update(long id, EstadoRequestDTO estado);
    void delete(long id);
    Estado findById(long id);
    Estado findBySigla(String sigla);
    List<Estado> findAll(int page, int pageSize);
    List<Estado> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
    
}
