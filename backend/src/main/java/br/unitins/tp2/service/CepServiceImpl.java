package br.unitins.tp2.service;

import br.unitins.tp2.client.ViaCepClient;
import br.unitins.tp2.dto.CepResponseDTO;
import br.unitins.tp2.dto.ViaCepResponseDTO;
import br.unitins.tp2.exception.CepUnavailableException;
import br.unitins.tp2.exception.ResourceNotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CepServiceImpl implements CepService {

    @Inject
    @RestClient
    ViaCepClient viaCepClient;

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

        return new CepResponseDTO(
                normalizedCep,
                response.logradouro(),
                response.bairro(),
                response.localidade(),
                response.uf());
    }
}
