package br.unitins.tp2.repository;

import br.unitins.tp2.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public PanacheQuery<Municipio> findByNome(String nome) {
        return find("nome like ?1 order by nome", "%" + nome + "%");
    }

    public Municipio findByNomeAndSigla(String nome, String sigla) {
        return find("lower(nome) = lower(?1) and upper(estado.sigla) = upper(?2)", nome, sigla)
                .firstResult();
    }

    @Override
    public PanacheQuery<Municipio> findAll() {
        return find("order by nome");
    }
}
