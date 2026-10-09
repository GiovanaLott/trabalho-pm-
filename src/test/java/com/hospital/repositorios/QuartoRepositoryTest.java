package com.hospital.repositorios;

import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class QuartoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private QuartoRepository quartoRepository;

    private Quarto quarto(String numero, int andar, int capacidade, int ocupacao, StatusQuarto situacao) {
        return em.persistAndFlush(new Quarto(null, numero, andar, capacidade, ocupacao, situacao));
    }

    @Test
    void quartosComVaga_excluemOsLotadosEFicamOrdenadosPorAndarENumero() {
        quarto("201", 2, 3, 0, StatusQuarto.DISPONIVEL);
        quarto("102", 1, 1, 1, StatusQuarto.OCUPADO);
        quarto("101", 1, 2, 1, StatusQuarto.DISPONIVEL);

        assertThat(quartoRepository.findComVagaDisponivel())
                .extracting(Quarto::getNumeroIdentificacao)
                .containsExactly("101", "201");
    }

    @Test
    void buscaPorSituacao() {
        quarto("101", 1, 2, 0, StatusQuarto.DISPONIVEL);
        quarto("102", 1, 1, 1, StatusQuarto.OCUPADO);

        assertThat(quartoRepository.findBySituacao(StatusQuarto.OCUPADO))
                .extracting(Quarto::getNumeroIdentificacao)
                .containsExactly("102");
    }

    @Test
    void buscaPorAndar_ficaOrdenadaPorNumero() {
        quarto("103", 1, 2, 0, StatusQuarto.DISPONIVEL);
        quarto("101", 1, 2, 0, StatusQuarto.DISPONIVEL);
        quarto("201", 2, 2, 0, StatusQuarto.DISPONIVEL);

        assertThat(quartoRepository.findByAndarOrderByNumeroIdentificacaoAsc(1))
                .extracting(Quarto::getNumeroIdentificacao)
                .containsExactly("101", "103");
    }

    @Test
    void buscaPorNumeroDeIdentificacao() {
        quarto("101", 1, 2, 0, StatusQuarto.DISPONIVEL);

        assertThat(quartoRepository.existsByNumeroIdentificacao("101")).isTrue();
        assertThat(quartoRepository.existsByNumeroIdentificacao("999")).isFalse();
        assertThat(quartoRepository.findByNumeroIdentificacao("101")).isPresent();
        assertThat(quartoRepository.findByNumeroIdentificacao("999")).isEmpty();
    }

    @Test
    void numeroDeIdentificacaoDuplicado_eRejeitadoPeloBanco() {
        quartoRepository.saveAndFlush(new Quarto(null, "101", 1, 2, 0, StatusQuarto.DISPONIVEL));

        assertThrows(DataIntegrityViolationException.class,
                () -> quartoRepository.saveAndFlush(new Quarto(null, "101", 2, 4, 0, StatusQuarto.DISPONIVEL)));
    }

    @Test
    void versaoAumentaACadaAtualizacao() {
        Quarto quarto = quarto("101", 1, 2, 0, StatusQuarto.DISPONIVEL);
        assertThat(quarto.getVersion()).isEqualTo(0L);

        quarto.incrementarOcupacao();
        em.flush();

        assertThat(quarto.getVersion()).isEqualTo(1L);
    }
}
