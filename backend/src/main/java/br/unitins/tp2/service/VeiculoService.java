package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.model.StatusVeiculo;
import br.unitins.tp2.model.Veiculo;

public interface VeiculoService {
    Veiculo buscar(long id);
    List<Veiculo> buscar(Integer anoModelo, String modelo, Long marcaId, StatusVeiculo status);
    List<Veiculo> listarDisponiveis(Integer anoModelo, String modelo, Long marcaId);
}
