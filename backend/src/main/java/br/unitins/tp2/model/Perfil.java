package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Perfil {
    ADMINISTRADOR(1L, "Administrador"),
    CLIENTE(2L, "Cliente");

    private final Long id;
    private final String nome;

    Perfil(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static Perfil valueOf(Long id) {
        if (id == null) {
            return null;
        }

        for (Perfil perfil : Perfil.values()) {
            if (Objects.equals(perfil.getId(), id)) {
                return perfil;
            }
        }
        return null;
    }
}
