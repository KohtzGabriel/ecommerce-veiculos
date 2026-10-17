package br.unitins.tp2.service;

import java.util.Locale;
import java.util.Map;

import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Estado;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.model.Regiao;
import br.unitins.tp2.repository.EstadoRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LocalidadeService {
    private static final Map<String, Regiao> REGIOES_POR_UF = Map.ofEntries(
            Map.entry("AC", Regiao.NORTE), Map.entry("AL", Regiao.NORDESTE), Map.entry("AP", Regiao.NORTE),
            Map.entry("AM", Regiao.NORTE), Map.entry("BA", Regiao.NORDESTE), Map.entry("CE", Regiao.NORDESTE),
            Map.entry("DF", Regiao.CENTRO_OESTE), Map.entry("ES", Regiao.SUDESTE), Map.entry("GO", Regiao.CENTRO_OESTE),
            Map.entry("MA", Regiao.NORDESTE), Map.entry("MT", Regiao.CENTRO_OESTE), Map.entry("MS", Regiao.CENTRO_OESTE),
            Map.entry("MG", Regiao.SUDESTE), Map.entry("PA", Regiao.NORTE), Map.entry("PB", Regiao.NORDESTE),
            Map.entry("PR", Regiao.SUL), Map.entry("PE", Regiao.NORDESTE), Map.entry("PI", Regiao.NORDESTE),
            Map.entry("RJ", Regiao.SUDESTE), Map.entry("RN", Regiao.NORDESTE), Map.entry("RS", Regiao.SUL),
            Map.entry("RO", Regiao.NORTE), Map.entry("RR", Regiao.NORTE), Map.entry("SC", Regiao.SUL),
            Map.entry("SP", Regiao.SUDESTE), Map.entry("SE", Regiao.NORDESTE), Map.entry("TO", Regiao.NORTE));

    @Inject EstadoRepository estados;
    @Inject MunicipioRepository municipios;

    @Transactional
    public Municipio resolverOuCriar(String cidade, String uf) {
        String nome = cidade == null ? "" : cidade.strip();
        String sigla = uf == null ? "" : uf.strip().toUpperCase(Locale.ROOT);
        if (nome.isBlank() || nome.length() > 80) {
            throw ValidationException.of("endereco.cidade", "Informe uma cidade válida.");
        }
        Regiao regiao = REGIOES_POR_UF.get(sigla);
        if (regiao == null) {
            throw ValidationException.of("endereco.estado", "Informe uma UF brasileira válida.");
        }

        Estado estado = estados.findBySigla(sigla);
        if (estado == null) {
            estado = new Estado();
            estado.setNome(sigla);
            estado.setSigla(sigla);
            estado.setRegiao(regiao);
            estados.persistAndFlush(estado);
        }

        Municipio municipio = municipios.findByNomeAndSigla(nome, sigla);
        if (municipio == null) {
            municipio = new Municipio();
            municipio.setNome(nome);
            municipio.setEstado(estado);
            municipios.persistAndFlush(municipio);
        }
        return municipio;
    }
}
