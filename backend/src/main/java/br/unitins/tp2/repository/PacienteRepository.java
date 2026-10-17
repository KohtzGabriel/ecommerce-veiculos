package br.unitins.tp2.repository;

import br.unitins.tp2.model.Paciente;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PacienteRepository implements PanacheRepository<Paciente> {

    public Paciente findByCpf(String cpf) {
        return find("pessoa.cpf", cpf).firstResult();
    }

    public PanacheQuery<Paciente> findByNome(String nome) {
        return find("lower(pessoa.nome) like lower(?1) order by pessoa.nome", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Paciente> findAll() {
        return find("order by pessoa.nome");
    }
}