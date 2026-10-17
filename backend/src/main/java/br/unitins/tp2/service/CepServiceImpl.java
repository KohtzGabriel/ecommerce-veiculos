package br.unitins.tp2.service;

import java.text.Normalizer;
import br.unitins.tp2.client.ViaCepClient;
import br.unitins.tp2.dto.CepResponseDTO;
import br.unitins.tp2.dto.ViaCepResponseDTO;
import br.unitins.tp2.exception.CepUnavailableException;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.repository.MunicipioRepository;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CepServiceImpl implements CepService {

    @Inject
    @RestClient
    ViaCepClient viaCepClient;

    @Inject
    MunicipioRepository municipioRepository;

    @Override
    public CepResponseDTO buscar(String cep) {
        final String normalizedCep;
        try {
            normalizedCep = CepNormalizer.normalize(cep);
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException(exception.getMessage());
        }

        final ViaCepResponseDTO response;
        try {
            response = viaCepClient.buscar(normalizedCep);
        } catch (RuntimeException exception) {
            throw new CepUnavailableException("A API ViaCEP não respondeu.", exception);
        }

        if (response == null) {
            throw new CepUnavailableException("A API ViaCEP retornou uma resposta vazia.", null);
        }
        if (Boolean.TRUE.equals(response.erro())) {
            throw new ResourceNotFoundException("CEP não encontrado.");
        }

        var municipio = localizarMunicipio(response.localidade(), response.uf());
        if (municipio == null) {
            throw new ResourceNotFoundException(
                    "O município retornado pelo CEP não está cadastrado. Preencha o endereço manualmente.");
        }

        return new CepResponseDTO(
                normalizedCep,
                response.logradouro(),
                response.bairro(),
                municipio.getId(),
                municipio.getNome(),
                municipio.getEstado().getSigla());
    }

    private br.unitins.tp2.model.Municipio localizarMunicipio(String nome, String sigla) {
        if (nome == null || sigla == null) {
            return null;
        }

        var municipio = municipioRepository.findByNomeAndSigla(nome, sigla);
        if (municipio != null) {
            return municipio;
        }

        var nomeNormalizado = normalizarTexto(nome);
        return municipioRepository.findAll().list().stream()
                .filter(item -> item.getEstado() != null
                        && sigla.equalsIgnoreCase(item.getEstado().getSigla())
                        && nomeNormalizado.equals(normalizarTexto(item.getNome())))
                .findFirst()
                .orElse(null);
    }

    private String normalizarTexto(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }
}
