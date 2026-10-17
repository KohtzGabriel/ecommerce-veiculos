package br.unitins.tp2.repository;

import java.util.List;

import br.unitins.tp2.model.StatusVeiculo;
import br.unitins.tp2.model.Veiculo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class VeiculoRepository implements PanacheRepository<Veiculo> {
    public List<Veiculo> buscar(Integer anoModelo, String modelo, Long marcaId, StatusVeiculo status,
            boolean somenteDisponiveis) {
        StringBuilder jpql = new StringBuilder("select v from Veiculo v where 1=1");
        if (anoModelo != null) jpql.append(" and v.anoModelo = :anoModelo");
        if (modelo != null && !modelo.isBlank()) jpql.append(" and lower(v.modelo) like :modelo");
        if (marcaId != null) jpql.append(" and v.marca.id = :marcaId");
        if (status != null) jpql.append(" and v.status = :status");
        if (somenteDisponiveis) jpql.append(" and v.ativo = true and v.status = :statusCatalogo");
        jpql.append(" order by v.modelo, v.id");

        TypedQuery<Veiculo> query = getEntityManager().createQuery(jpql.toString(), Veiculo.class);
        if (anoModelo != null) query.setParameter("anoModelo", anoModelo);
        if (modelo != null && !modelo.isBlank()) query.setParameter("modelo", "%" + modelo.strip().toLowerCase() + "%");
        if (marcaId != null) query.setParameter("marcaId", marcaId);
        if (status != null) query.setParameter("status", status);
        if (somenteDisponiveis) query.setParameter("statusCatalogo", StatusVeiculo.DISPONIVEL);
        return query.getResultList();
    }
}
