package br.unitins.tp2.repository;

import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.model.PessoaFisica;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {
    public Cliente findByCpf(String cpf) {
        return getEntityManager().createQuery(
                "select c from Cliente c join fetch c.pessoa p where treat(p as PessoaFisica).cpf = :cpf",
                Cliente.class)
                .setParameter("cpf", cpf).getResultStream().findFirst().orElse(null);
    }

    public boolean hasVenda(Long clienteId) {
        return getEntityManager().createQuery(
                "select count(n) from NotaFiscal n where n.cliente.id = :clienteId and n.entrada = false", Long.class)
                .setParameter("clienteId", clienteId).getSingleResult() > 0;
    }
}
