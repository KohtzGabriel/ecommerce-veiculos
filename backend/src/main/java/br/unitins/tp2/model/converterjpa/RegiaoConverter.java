package br.unitins.tp2.model.converterjpa;

import br.unitins.tp2.model.Regiao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RegiaoConverter implements AttributeConverter<Regiao, Long> {

    @Override
    public Long convertToDatabaseColumn(Regiao regiao) {
        return regiao == null ? null : regiao.getId();

    }

    @Override
    public Regiao convertToEntityAttribute(Long id) {
        return Regiao.valueOf(id);
    }
    
}
