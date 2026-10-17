package br.unitins.tp2.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
@Entity public class Adicional extends Produto {
 @Column(length=40,nullable=false,unique=true) private String codigo;
 @Column(nullable=false) private Integer quantidadeEstoque=0;
 public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;}
 public Integer getQuantidadeEstoque(){return quantidadeEstoque;} public void setQuantidadeEstoque(Integer v){quantidadeEstoque=v;}
}
