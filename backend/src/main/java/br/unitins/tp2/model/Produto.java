package br.unitins.tp2.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Produto extends DefaultEntity {
    @Column(length=500, nullable=false) private String descricao;
    @Column(precision=19, scale=2, nullable=false) private BigDecimal valorCusto;
    @Column(precision=19, scale=2, nullable=false) private BigDecimal valorVenda;
    @Column(nullable=false) private boolean ativo=true;
    public String getDescricao(){return descricao;} public void setDescricao(String v){descricao=v;}
    public BigDecimal getValorCusto(){return valorCusto;} public void setValorCusto(BigDecimal v){valorCusto=v;}
    public BigDecimal getValorVenda(){return valorVenda;} public void setValorVenda(BigDecimal v){valorVenda=v;}
    public boolean isAtivo(){return ativo;} public void setAtivo(boolean v){ativo=v;}
}
