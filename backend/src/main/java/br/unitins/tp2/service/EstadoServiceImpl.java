package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.EstadoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Estado;
import br.unitins.tp2.model.Regiao;
import br.unitins.tp2.repository.EstadoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EstadoServiceImpl implements EstadoService {

    @Inject
    EstadoRepository estadoRepository;

    @Override
    @Transactional
    public Estado create(EstadoRequestDTO estado) {
        validarDados(estado, null);
        Estado novoEstado = new Estado();
        aplicarDados(novoEstado, estado);

        estadoRepository.persist(novoEstado);

        return novoEstado;
    }

    private void validarDados(EstadoRequestDTO dto, Long id) {
        Estado estado = estadoRepository.findBySiglaExceptId(dto.sigla(), id);
        if (estado != null)
           throw ValidationException.of("sigla", "Já existe um estado cadastrado com essa sigla.");
    }

    @Override
    @Transactional
    public void update(long id, EstadoRequestDTO estado) {
        validarDados(estado, id);
        Estado edicaoEstado = buscarEstadoOuFalhar(id);
        aplicarDados(edicaoEstado, estado);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!estadoRepository.deleteById(id))
            throw new ResourceNotFoundException("Estado não encontrado.");
    }

    @Override
    public Estado findById(long id) {
        return buscarEstadoOuFalhar(id);
    }

    @Override
    public Estado findBySigla(String sigla) {
        Estado estado = estadoRepository.findBySigla(sigla);
        if (estado == null)
            throw new ResourceNotFoundException("Estado não encontrado.");

        return estado;
    }

    @Override
    public List<Estado> findAll(int page, int pageSize) {
        return estadoRepository.findAll().page(page, pageSize).list();
        
    }

    @Override
    public List<Estado> findByNome(String nome, int page, int pageSize) {
        return estadoRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return estadoRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return estadoRepository.findByNome(nome).count();
    }

    private void aplicarDados(Estado destino, EstadoRequestDTO origem) {
        destino.setNome(origem.nome());
        destino.setSigla(origem.sigla());
        destino.setRegiao(buscarRegiaoOuFalhar(origem.idRegiao()));
    }

    private Estado buscarEstadoOuFalhar(long id) {
        Estado estado = estadoRepository.findById(id);
        if (estado == null)
            throw new ResourceNotFoundException("Estado não encontrado.");

        return estado;
    }

    private Regiao buscarRegiaoOuFalhar(Long idRegiao) {
        Regiao regiao = Regiao.valueOf(idRegiao);
        if (regiao == null)
            throw ValidationException.of("idRegiao", "Região não encontrada.");

        return regiao;
    }

}
