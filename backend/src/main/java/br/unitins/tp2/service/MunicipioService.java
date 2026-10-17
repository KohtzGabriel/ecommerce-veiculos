package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.MunicipioRequestDTO;
import br.unitins.tp2.model.Municipio;
import jakarta.validation.Valid;

public interface MunicipioService {

    Municipio create(@Valid MunicipioRequestDTO municipio);
    void update(long id, MunicipioRequestDTO municipio);
    void delete(long id);
    Municipio findById(long id);
    List<Municipio> findAll(int page, int pageSize);
    List<Municipio> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}