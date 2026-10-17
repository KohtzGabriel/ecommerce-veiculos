package br.unitins.tp2.model;
import jakarta.persistence.*;
@Entity public class Veiculo extends Produto {
 @Column(length=100,nullable=false) private String modelo;
 @Column(nullable=false) private Integer anoFabricacao;
 @Column(nullable=false) private Integer anoModelo;
 @Column(length=40,nullable=false) private String cor;
 @Column(length=10,unique=true) private String placa;
 @Column(length=17,nullable=false,unique=true) private String chassi;
 @Column(length=11,unique=true) private String renavam;
 @Column(nullable=false) private Integer quilometragem=0;
 @ManyToOne(optional=false) @JoinColumn(name="marca_id",nullable=false) private Marca marca;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatusVeiculo status=StatusVeiculo.DISPONIVEL;
 public String getModelo(){return modelo;} public void setModelo(String v){modelo=v;}
 public Integer getAnoFabricacao(){return anoFabricacao;} public void setAnoFabricacao(Integer v){anoFabricacao=v;}
 public Integer getAnoModelo(){return anoModelo;} public void setAnoModelo(Integer v){anoModelo=v;}
 public String getCor(){return cor;} public void setCor(String v){cor=v;}
 public String getPlaca(){return placa;} public void setPlaca(String v){placa=v;}
 public String getChassi(){return chassi;} public void setChassi(String v){chassi=v;}
 public String getRenavam(){return renavam;} public void setRenavam(String v){renavam=v;}
 public Integer getQuilometragem(){return quilometragem;} public void setQuilometragem(Integer v){quilometragem=v;}
 public Marca getMarca(){return marca;} public void setMarca(Marca v){marca=v;}
 public StatusVeiculo getStatus(){return status;} public void setStatus(StatusVeiculo v){status=v;}
}
