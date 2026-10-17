package br.unitins.tp2.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.unitins.tp2.model.converterjpa.PerfilConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Usuario extends DefaultEntity {

    @Column(length = 120, nullable = false, unique = true)
    private String login;

    // Deve armazenar o hash da senha, nunca a senha em texto puro.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 255, nullable = false)
    private String senha;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    private Pessoa pessoa;

    @Column(nullable = false)
    private boolean ativo = true;

    @JdbcTypeCode(SqlTypes.ARRAY)
    // No array, explicita a conversão dos elementos para salvar os IDs, não os ordinais.
    @Convert(converter = PerfilConverter.class)
    @Column(name = "perfis", nullable = false)
    private Set<Perfil> perfis = new HashSet<>();

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<Perfil> perfis) {
        Set<Perfil> novosPerfis = perfis == null ? new HashSet<>() : new HashSet<>(perfis);
        this.perfis.clear();
        this.perfis.addAll(novosPerfis);
    }
}
