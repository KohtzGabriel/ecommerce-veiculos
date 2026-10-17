package br.unitins.tp2.model.converterjpa;

import br.unitins.tp2.model.Perfil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PerfilConverter implements AttributeConverter<Perfil, Long> {

    @Override
    public Long convertToDatabaseColumn(Perfil perfil) {
        return perfil == null ? null : perfil.getId();
    }

    @Override
    public Perfil convertToEntityAttribute(Long id) {
        return Perfil.valueOf(id);
    }
}
