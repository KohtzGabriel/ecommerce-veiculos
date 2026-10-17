package br.unitins.tp2.model;
import jakarta.persistence.*;
@Entity public class Fornecedor extends DefaultEntity {
 @Column(nullable=false) private boolean ativo=true;
 @OneToOne(optional=false) @JoinColumn(name="pessoa_juridica_id",nullable=false,unique=true) private PessoaJuridica pessoaJuridica;
 public boolean isAtivo(){return ativo;} public void setAtivo(boolean v){ativo=v;}
 public PessoaJuridica getPessoaJuridica(){return pessoaJuridica;} public void setPessoaJuridica(PessoaJuridica v){pessoaJuridica=v;}
}
