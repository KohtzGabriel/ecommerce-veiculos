package br.unitins.tp2.repository;

import br.unitins.tp2.model.Psicologo;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PsicologoRepository implements PanacheRepository<Psicologo> {

    public Psicologo findByCpf(String cpf) {
        return find("pessoa.cpf", cpf).firstResult();
    }

    public PanacheQuery<Psicologo> findByNome(String nome) {
        return find("lower(pessoa.nome) like lower(?1) order by pessoa.nome", "%" + nome + "%");
    }

    public Psicologo findByCrp(String crp) {
        return find("crp", crp).firstResult();
    }

    public Psicologo findByCrpExceptId(String crp, Long id) {
        if (id == null) {
            return findByCrp(crp);
        }

        return find("crp = ?1 and id <> ?2", crp, id).firstResult();
    }

    @Override
    public PanacheQuery<Psicologo> findAll() {
        return find("order by pessoa.nome");
    }
}