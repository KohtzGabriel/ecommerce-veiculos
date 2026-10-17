package br.unitins.tp2.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
@Entity public class NotaFiscal extends DefaultEntity {
 @Column(length=40,nullable=false) private String numero;
 @Column(length=10,nullable=false) private String serie;
 @Column(nullable=false) private LocalDate dataEmissao;
 @Column(nullable=false) private boolean entrada;
 @Column(precision=19,scale=2,nullable=false) private BigDecimal valorTotal=BigDecimal.ZERO;
 @ManyToOne @JoinColumn(name="fornecedor_id") private Fornecedor fornecedor;
 @ManyToOne @JoinColumn(name="cliente_id") private Cliente cliente;
 @OneToMany(mappedBy="notaFiscal",cascade=CascadeType.ALL,orphanRemoval=true) private List<ItemNotaFiscal> itens=new ArrayList<>();
 public String getNumero(){return numero;} public void setNumero(String v){numero=v;}
 public String getSerie(){return serie;} public void setSerie(String v){serie=v;}
 public LocalDate getDataEmissao(){return dataEmissao;} public void setDataEmissao(LocalDate v){dataEmissao=v;}
 public boolean isEntrada(){return entrada;} public void setEntrada(boolean v){entrada=v;}
 public BigDecimal getValorTotal(){return valorTotal;} public void setValorTotal(BigDecimal v){valorTotal=v;}
 public Fornecedor getFornecedor(){return fornecedor;} public void setFornecedor(Fornecedor v){fornecedor=v;}
 public Cliente getCliente(){return cliente;} public void setCliente(Cliente v){cliente=v;}
 public List<ItemNotaFiscal> getItens(){return itens;}
 public void substituirItens(List<ItemNotaFiscal> itens){this.itens.clear();if(itens!=null)for(var i:itens){i.setNotaFiscal(this);this.itens.add(i);} recalcularTotal();}
 public void recalcularTotal(){valorTotal=itens.stream().map(ItemNotaFiscal::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
}
