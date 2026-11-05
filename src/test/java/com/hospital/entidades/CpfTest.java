package com.hospital.entidades;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35", " 390.533.447-05 "})
    @DisplayName("Aceita CPFs válidos com ou sem máscara")
    void deveAceitarCpfsValidos(String cpf) {
        assertTrue(Cpf.isValido(cpf));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123.456.789-00", "111.111.111-11", "000.000.000-00", "5299822472", "529982247250", "529.982.247-2X", "abc"})
    @DisplayName("Rejeita CPFs vazios, com dígito verificador errado, sequências repetidas ou tamanho incorreto")
    void deveRejeitarCpfsInvalidos(String cpf) {
        assertFalse(Cpf.isValido(cpf));
    }

    @Test
    @DisplayName("Formata o CPF no padrão 000.000.000-00")
    void deveFormatarCpf() {
        assertEquals("529.982.247-25", Cpf.formatar("52998224725"));
        assertEquals("529.982.247-25", Cpf.formatar("529.982.247-25"));
    }
}
