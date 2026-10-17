package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Regiao {
    CENTRO_OESTE(1l, "Centro-Oeste"), 
    NORDESTE(3l, "Nordeste"), 
    NORTE(2l, "Norte"), 
    SUDESTE(4l, "Sudeste"), 
    SUL(5l, "Sul");

    private final Long ID;
    private final String NOME;

    Regiao(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

     public static Regiao valueOf(Long id) {
        if (id == null)
            return null;

        for (Regiao r : Regiao.values()) {
            if (Objects.equals(r.getId(), id))
                return r;
        }
        return null;
     }

}
