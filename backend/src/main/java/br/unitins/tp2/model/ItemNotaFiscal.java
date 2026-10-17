package br.unitins.tp2.model;
import java.math.BigDecimal;
import jakarta.persistence.*;
@Entity public class ItemNotaFiscal extends DefaultEntity {
 @Column(nullable=false) private Integer quantidade;
 @Column(precision=19,scale=2,nullable=false) private BigDecimal valorUnitario;
 @ManyToOne(optional=false) @JoinColumn(name="nota_fiscal_id",nullable=false) private NotaFiscal notaFiscal;
 @ManyToOne(optional=false) @JoinColumn(name="produto_id",nullable=false) private Produto produto;
 public Integer getQuantidade(){return quantidade;} public void setQuantidade(Integer v){quantidade=v;}
 public BigDecimal getValorUnitario(){return valorUnitario;} public void setValorUnitario(BigDecimal v){valorUnitario=v;}
 public BigDecimal getSubtotal(){return valorUnitario==null||quantidade==null?BigDecimal.ZERO:valorUnitario.multiply(BigDecimal.valueOf(quantidade));}
 public NotaFiscal getNotaFiscal(){return notaFiscal;} public void setNotaFiscal(NotaFiscal v){notaFiscal=v;}
 public Produto getProduto(){return produto;} public void setProduto(Produto v){produto=v;}
}
