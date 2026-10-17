package br.unitins.tp2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.type.BasicPluralType;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.converter.spi.BasicValueConverter;
import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.converterjpa.PerfilConverter;

class UsuarioMappingTest {

    @Test
    @SuppressWarnings("unchecked")
    void deveMapearPerfisComoArrayDeIdsSemTabelaDeColecao() {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .build();

        try {
            var metadata = new MetadataSources(registry)
                    .addAnnotatedClass(Usuario.class)
                    .addAnnotatedClass(Pessoa.class)
                    .addAnnotatedClass(PessoaFisica.class)
                    .addAnnotatedClass(PessoaJuridica.class)
                    .addAnnotatedClass(Endereco.class)
                    .addAnnotatedClass(Municipio.class)
                    .addAnnotatedClass(Estado.class)
                    .addAnnotatedClass(Telefone.class)
                    .getMetadataBuilder()
                    .applyAttributeConverter(PerfilConverter.class, true)
                    .build();

            var property = metadata.getEntityBinding(Usuario.class.getName()).getProperty("perfis");
            var type = assertInstanceOf(BasicPluralType.class, property.getType());
            assertEquals(SqlTypes.ARRAY, type.getJdbcType().getJdbcTypeCode());
            assertEquals(SqlTypes.BIGINT, type.getElementType().getJdbcType().getJdbcTypeCode());
            assertEquals("perfis", property.getColumns().getFirst().getName());
            assertEquals(0, metadata.getCollectionBindings().stream()
                    .filter(binding -> binding.getRole().equals(Usuario.class.getName() + ".perfis"))
                    .count());

            var converter = (BasicValueConverter<Set<Perfil>, Long[]>) type.getValueConverter();
            assertNotNull(converter);
            var perfis = Set.of(Perfil.ADMINISTRADOR, Perfil.CLIENTE);
            var ids = converter.toRelationalValue(perfis);
            assertEquals(Set.of(1L, 2L), Set.of(ids));
            assertEquals(perfis, converter.toDomainValue(ids));
            assertEquals(0, converter.toRelationalValue(Set.of()).length);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}

