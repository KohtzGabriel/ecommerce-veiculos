package br.unitins.tp2.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
@Entity public class Marca extends DefaultEntity {
 @Column(length=80,nullable=false,unique=true) private String nome;
 public String getNome(){return nome;} public void setNome(String v){nome=v;}
}
