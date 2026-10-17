package br.unitins.tp2.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Psicologo extends DefaultEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    private Pessoa pessoa;

    @Column(length = 20, nullable = false, unique = true)
    private String crp;

    @Column(length = 120, nullable = false)
    private String especialidade;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorConsulta;

    @Column(nullable = false)
    private Integer duracaoConsulta;

    @Column(nullable = false)
    private boolean ativo = true;

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public String getCrp() {
        return crp;
    }

    public void setCrp(String crp) {
        this.crp = crp;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public BigDecimal getValorConsulta() {
        return valorConsulta;
    }

    public void setValorConsulta(BigDecimal valorConsulta) {
        this.valorConsulta = valorConsulta;
    }

    public Integer getDuracaoConsulta() {
        return duracaoConsulta;
    }

    public void setDuracaoConsulta(Integer duracaoConsulta) {
        this.duracaoConsulta = duracaoConsulta;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

}