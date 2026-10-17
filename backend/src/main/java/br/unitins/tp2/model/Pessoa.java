package br.unitins.tp2.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import jakarta.persistence.OneToMany;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Pessoa extends DefaultEntity {

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(length = 120, nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telefone> telefones = new ArrayList<>();

    @Column(length = 30)
    private String genero;

    // Campo textual mantido para compatibilidade com cadastros clínicos legados.
    @Column(name = "endereco_legado", length = 255)
    private String enderecoLegado;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id", unique = true)
    private Endereco endereco;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Telefone> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones.clear();

        if (telefones == null) {
            return;
        }

        for (Telefone telefone : telefones) {
            telefone.setPessoa(this);
            this.telefones.add(telefone);
        }
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getEnderecoLegado() { return enderecoLegado; }
    public void setEnderecoLegado(String enderecoLegado) { this.enderecoLegado = enderecoLegado; }

    /** Compatibilidade temporária para os recursos clínicos que só atendem pessoas físicas. */
    @Deprecated public String getCpf() { return this instanceof PessoaFisica pf ? pf.getCpf() : null; }
    @Deprecated public void setCpf(String cpf) { if (this instanceof PessoaFisica pf) pf.setCpf(cpf); }
    @Deprecated public LocalDate getDataNascimento() { return this instanceof PessoaFisica pf ? pf.getDataNascimento() : null; }
    @Deprecated public void setDataNascimento(LocalDate data) { if (this instanceof PessoaFisica pf) pf.setDataNascimento(data); }

}
