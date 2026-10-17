package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.MunicipioRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Estado;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.repository.EstadoRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MunicipioServiceImpl implements MunicipioService {

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    EstadoRepository estadoRepository;

    @Override
    @Transactional
    public Municipio create(MunicipioRequestDTO municipio) {
        Municipio novoMunicipio = new Municipio();
        aplicarDados(novoMunicipio, municipio);

        municipioRepository.persist(novoMunicipio);

        return novoMunicipio;
    }

    @Override
    @Transactional
    public void update(long id, MunicipioRequestDTO municipio) {
        Municipio edicaoMunicipio = buscarMunicipioOuFalhar(id);
        aplicarDados(edicaoMunicipio, municipio);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!municipioRepository.deleteById(id))
            throw new ResourceNotFoundException("Município não encontrado.");
    }

    @Override
    public Municipio findById(long id) {
        return buscarMunicipioOuFalhar(id);
    }

    @Override
    public List<Municipio> findAll(int page, int pageSize) {
        return municipioRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Municipio> findByNome(String nome, int page, int pageSize) {
        return municipioRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return municipioRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return municipioRepository.findByNome(nome).count();
    }

    private void aplicarDados(Municipio destino, MunicipioRequestDTO origem) {
        destino.setNome(origem.nome());
        destino.setEstado(buscarEstadoOuFalhar(origem.idEstado()));
    }

    private Estado buscarEstadoOuFalhar(Long idEstado) {
        Estado estado = estadoRepository.findById(idEstado);
        if (estado == null)
            throw ValidationException.of("idEstado", "Estado não encontrado.");

        return estado;
    }

    private Municipio buscarMunicipioOuFalhar(long id) {
        Municipio municipio = municipioRepository.findById(id);
        if (municipio == null)
            throw new ResourceNotFoundException("Município não encontrado.");

        return municipio;
    }
}