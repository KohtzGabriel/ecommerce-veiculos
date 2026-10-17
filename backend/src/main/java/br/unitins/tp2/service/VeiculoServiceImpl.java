package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.model.StatusVeiculo;
import br.unitins.tp2.model.Veiculo;
import br.unitins.tp2.repository.VeiculoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VeiculoServiceImpl implements VeiculoService {
    @Inject VeiculoRepository veiculos;

    @Override
    @Transactional
    public Veiculo buscar(long id) {
        var veiculo = veiculos.findById(id);
        if (veiculo == null) throw new ResourceNotFoundException("Veículo não encontrado.");
        return veiculo;
    }

    @Override
    @Transactional
    public List<Veiculo> buscar(Integer anoModelo, String modelo, Long marcaId, StatusVeiculo status) {
        return veiculos.buscar(anoModelo, modelo, marcaId, status, false);
    }

    @Override
    @Transactional
    public List<Veiculo> listarDisponiveis(Integer anoModelo, String modelo, Long marcaId) {
        return veiculos.buscar(anoModelo, modelo, marcaId, null, true);
    }
}
