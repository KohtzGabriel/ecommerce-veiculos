package br.unitins.tp2.repository;
import br.unitins.tp2.model.Fornecedor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped public class FornecedorRepository implements PanacheRepository<Fornecedor> {
    public boolean hasNotaFiscal(Long fornecedorId) {
        return getEntityManager().createQuery(
                "select count(n) from NotaFiscal n where n.fornecedor.id = :fornecedorId", Long.class)
                .setParameter("fornecedorId", fornecedorId).getSingleResult() > 0;
    }
}
