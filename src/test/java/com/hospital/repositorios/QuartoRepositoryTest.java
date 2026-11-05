package com.hospital.repositorios;

import com.hospital.entidades.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QuartoRepositoryTest {

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persisteERecuperaCadaTipoComSuaSubclasse() {
        Long idApto = quartoRepository.save(new Apartamento(null, "AP-201", 2, 1, 0, null, false)).getId();
        Long idIso = quartoRepository.save(new QuartoIsolamento(null, "ISO-401", 4, 0, null, TipoIsolamento.AEROSSOIS)).getId();
        Long idUti = quartoRepository.save(new UnidadeTerapiaIntensiva(null, "UTI-301", 3, 6, 0, null)).getId();
        entityManager.flush();
        entityManager.clear();

        Quarto apto = quartoRepository.findById(idApto).orElseThrow();
        assertInstanceOf(Apartamento.class, apto);
        assertFalse(((Apartamento) apto).getPermiteAcompanhante());
        assertEquals(TipoIsolamento.AEROSSOIS, ((QuartoIsolamento) quartoRepository.findById(idIso).orElseThrow()).getTipoIsolamento());
        assertEquals(TipoQuarto.UTI, quartoRepository.findById(idUti).orElseThrow().getTipo());

        String tipoGravado = jdbcTemplate.queryForObject("select tipo from quartos where id = ?", String.class, idUti);
        assertEquals("UTI", tipoGravado);
    }

    @Test
    void filtraPorSituacaoEOrdenaPorAndarENumero() {
        quartoRepository.save(new Enfermaria(null, "E-302", 3, 2, 2, StatusQuarto.OCUPADO));
        quartoRepository.save(new Enfermaria(null, "E-101", 1, 4, 0, StatusQuarto.DISPONIVEL));
        quartoRepository.save(new Enfermaria(null, "E-301", 3, 4, 1, StatusQuarto.DISPONIVEL));

        assertEquals(2, quartoRepository.findBySituacao(StatusQuarto.DISPONIVEL).size());
        List<String> ordem = quartoRepository.findAllByOrderByAndarAscNumeroIdentificacaoAsc().stream()
                .map(Quarto::getNumeroIdentificacao).toList();
        assertEquals(List.of("E-101", "E-301", "E-302"), ordem);
    }

    @Test
    void verificaNumeroDuplicadoIgnorandoOProprioQuarto() {
        Quarto quarto = quartoRepository.save(new Enfermaria(null, "E-500", 5, 2, 0, null));
        assertTrue(quartoRepository.existsByNumeroIdentificacao("E-500"));
        assertFalse(quartoRepository.existsByNumeroIdentificacaoAndIdNot("E-500", quarto.getId()));
        assertTrue(quartoRepository.findByNumeroIdentificacao("E-500").isPresent());
    }

    @Test
    void versaoEhIncrementadaACadaAlteracao() {
        Quarto quarto = quartoRepository.saveAndFlush(new Enfermaria(null, "E-600", 6, 2, 0, null));
        Long versaoInicial = quarto.getVersao();
        assertNotNull(versaoInicial);

        quarto.incrementarOcupacao();
        quartoRepository.saveAndFlush(quarto);

        assertEquals(versaoInicial + 1, quarto.getVersao());
    }
}
