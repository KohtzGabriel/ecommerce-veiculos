package br.unitins.tp2.repository;

import br.unitins.tp2.model.Pessoa;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PessoaRepository implements PanacheRepository<Pessoa> {

    public Pessoa findByCpf(String cpf) {
        return getEntityManager().createQuery("select p from PessoaFisica p where p.cpf = :cpf", Pessoa.class)
                .setParameter("cpf", cpf).getResultStream().findFirst().orElse(null);
    }

    public Pessoa findByCpfExceptId(String cpf, Long idPessoa) {
        if (idPessoa == null) {
            return findByCpf(cpf);
        }

        return getEntityManager().createQuery("select p from PessoaFisica p where p.cpf = :cpf and p.id <> :id", Pessoa.class)
                .setParameter("cpf", cpf).setParameter("id", idPessoa).getResultStream().findFirst().orElse(null);
    }

    public Pessoa findByEmailExceptId(String email, Long idPessoa) {
        if (idPessoa == null) {
            return find("email", email).firstResult();
        }

        return find("email = ?1 and id <> ?2", email, idPessoa).firstResult();
    }
}
