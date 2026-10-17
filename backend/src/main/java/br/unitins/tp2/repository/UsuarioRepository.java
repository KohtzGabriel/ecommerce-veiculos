package br.unitins.tp2.repository;

import br.unitins.tp2.model.Usuario;
import java.util.List;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public List<Usuario> listarUsuarios(String nome, int page, int pageSize) {
        String filtro = nome == null ? "" : " where lower(p.nome) like lower(:nome)";
        var query = getEntityManager().createQuery(
                "select u from Usuario u join fetch u.pessoa p" + filtro
                        + " order by p.nome, p.id", Usuario.class);
        if (nome != null) {
            query.setParameter("nome", "%" + nome + "%");
        }
        return query.setFirstResult(Math.multiplyExact(page, pageSize)).setMaxResults(pageSize)
                .getResultList();
    }

    public long contarUsuarios(String nome) {
        String filtro = nome == null ? "" : " where lower(p.nome) like lower(:nome)";
        var query = getEntityManager().createQuery("select count(u) from Usuario u join u.pessoa p" + filtro,
                Long.class);
        if (nome != null) {
            query.setParameter("nome", "%" + nome + "%");
        }
        return query.getSingleResult();
    }

    public Usuario findByLogin(String login) {
        return find("lower(login) = lower(?1)", login).firstResult();
    }

    public Usuario findByPessoaId(Long pessoaId) {
        return find("pessoa.id", pessoaId).firstResult();
    }

    public Usuario findByCpf(String cpf) {
        return getEntityManager().createQuery(
                "select u from Usuario u join u.pessoa p where treat(p as PessoaFisica).cpf = :cpf", Usuario.class)
                .setParameter("cpf", cpf).getResultStream().findFirst().orElse(null);
    }
}
