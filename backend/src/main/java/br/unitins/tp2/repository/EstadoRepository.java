package br.unitins.tp2.repository;

import br.unitins.tp2.model.Estado;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EstadoRepository implements PanacheRepository<Estado> {

    public Estado findBySigla(String sigla) {
        return find("sigla", sigla).firstResult();
    }

    public Estado findBySiglaExceptId(String sigla, Long id) {
        if (id == null)
            return findBySigla(sigla);
        return find("sigla = ?1 and id <> ?2", sigla, id).firstResult();
    }

    public PanacheQuery<Estado> findByNome(String nome) {
        return find("nome like ?1 order by nome", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Estado> findAll() {
       return find("order by nome");
    }

}