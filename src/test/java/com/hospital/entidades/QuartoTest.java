package com.hospital.entidades;

import com.hospital.excecoes.QuartoIndisponivelException;
import com.hospital.excecoes.QuartoLotadoException;
import com.hospital.excecoes.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuartoTest {

    private Internacao internacaoCom(String observacoes) {
        Internacao internacao = new Internacao();
        internacao.setObservacoes(observacoes);
        return internacao;
    }

    @Nested
    @DisplayName("Tipos de quarto")
    class Tipos {

        @Test
        @DisplayName("Cada tipo cria a subclasse correspondente")
        void fabricaDeTipos() {
            assertInstanceOf(Enfermaria.class, TipoQuarto.ENFERMARIA.novoQuarto());
            assertInstanceOf(Apartamento.class, TipoQuarto.APARTAMENTO.novoQuarto());
            assertInstanceOf(UnidadeTerapiaIntensiva.class, TipoQuarto.UTI.novoQuarto());
            assertInstanceOf(QuartoIsolamento.class, TipoQuarto.ISOLAMENTO.novoQuarto());
            for (TipoQuarto tipo : TipoQuarto.values()) {
                assertEquals(tipo, tipo.novoQuarto().getTipo());
            }
        }

        @Test
        @DisplayName("Enfermaria deve ter entre 2 e 8 leitos")
        void limitesEnfermaria() {
            assertThrows(RegraNegocioException.class,
                    () -> new Enfermaria(null, "E-1", 1, 1, 0, null).validarConfiguracao());
            assertThrows(RegraNegocioException.class,
                    () -> new Enfermaria(null, "E-1", 1, 9, 0, null).validarConfiguracao());
            assertDoesNotThrow(() -> new Enfermaria(null, "E-1", 1, 6, 0, null).validarConfiguracao());
        }

        @Test
        @DisplayName("Apartamento deve ter no máximo 2 leitos e permite acompanhante por padrão")
        void limitesApartamento() {
            assertThrows(RegraNegocioException.class,
                    () -> new Apartamento(null, "A-1", 2, 3, 0, null, null).validarConfiguracao());
            Apartamento apartamento = new Apartamento(null, "A-1", 2, 1, 0, null, null);
            assertDoesNotThrow(apartamento::validarConfiguracao);
            assertTrue(apartamento.getPermiteAcompanhante());
        }

        @Test
        @DisplayName("UTI deve ter entre 1 e 10 leitos")
        void limitesUti() {
            assertThrows(RegraNegocioException.class,
                    () -> new UnidadeTerapiaIntensiva(null, "U-1", 3, 11, 0, null).validarConfiguracao());
            assertDoesNotThrow(() -> new UnidadeTerapiaIntensiva(null, "U-1", 3, 10, 0, null).validarConfiguracao());
        }

        @Test
        @DisplayName("Isolamento tem exatamente 1 leito e exige o tipo de isolamento")
        void regrasIsolamento() {
            QuartoIsolamento semTipo = new QuartoIsolamento(null, "I-1", 4, 0, null, null);
            RegraNegocioException erro = assertThrows(RegraNegocioException.class, semTipo::validarConfiguracao);
            assertTrue(erro.getMessage().contains("tipo de isolamento"));

            QuartoIsolamento isolamento = new QuartoIsolamento(null, "I-1", 4, 0, null, TipoIsolamento.AEROSSOIS);
            assertEquals(1, isolamento.getCapacidadeMaxima());
            assertDoesNotThrow(isolamento::validarConfiguracao);

            isolamento.setCapacidadeMaxima(2);
            assertThrows(RegraNegocioException.class, isolamento::validarConfiguracao);
        }

        @Test
        @DisplayName("Número de identificação e andar são obrigatórios")
        void camposObrigatorios() {
            assertThrows(RegraNegocioException.class,
                    () -> new Enfermaria(null, " ", 1, 2, 0, null).validarConfiguracao());
            assertThrows(RegraNegocioException.class,
                    () -> new Enfermaria(null, "E-1", null, 2, 0, null).validarConfiguracao());
            assertThrows(RegraNegocioException.class,
                    () -> new Enfermaria(null, "E-1", -1, 2, 0, null).validarConfiguracao());
        }
    }

    @Nested
    @DisplayName("Ocupação e admissão")
    class Ocupacao {

        @Test
        @DisplayName("Ocupa leitos até a capacidade máxima e então passa para OCUPADO")
        void ocupaAteLotar() {
            Quarto quarto = new Enfermaria(1L, "E-1", 1, 2, 0, StatusQuarto.DISPONIVEL);

            quarto.registrarAdmissao(internacaoCom(null));
            assertEquals(1, quarto.getOcupacaoAtual());
            assertEquals(StatusQuarto.DISPONIVEL, quarto.getSituacao());
            assertEquals(1, quarto.getLeitosDisponiveis());

            quarto.registrarAdmissao(internacaoCom(null));
            assertEquals(2, quarto.getOcupacaoAtual());
            assertEquals(StatusQuarto.OCUPADO, quarto.getSituacao());
            assertEquals(0, quarto.getLeitosDisponiveis());
        }

        @Test
        @DisplayName("Não ultrapassa a capacidade máxima (regra 5)")
        void naoUltrapassaCapacidade() {
            Quarto quarto = new Apartamento(1L, "A-1", 2, 1, 1, StatusQuarto.OCUPADO, true);
            assertThrows(QuartoLotadoException.class, () -> quarto.registrarAdmissao(internacaoCom(null)));
            assertEquals(1, quarto.getOcupacaoAtual());
        }

        @Test
        @DisplayName("Liberar um leito de quarto lotado o torna disponível novamente")
        void liberaLeito() {
            Quarto quarto = new Apartamento(1L, "A-1", 2, 1, 1, StatusQuarto.OCUPADO, true);
            quarto.decrementarOcupacao();
            assertEquals(0, quarto.getOcupacaoAtual());
            assertEquals(StatusQuarto.DISPONIVEL, quarto.getSituacao());
        }

        @Test
        @DisplayName("UTI exige justificativa clínica nas observações da internação")
        void utiExigeJustificativa() {
            Quarto uti = new UnidadeTerapiaIntensiva(1L, "U-1", 3, 4, 0, StatusQuarto.DISPONIVEL);
            assertThrows(RegraNegocioException.class, () -> uti.registrarAdmissao(internacaoCom(null)));
            assertThrows(RegraNegocioException.class, () -> uti.registrarAdmissao(internacaoCom("   ")));
            assertEquals(0, uti.getOcupacaoAtual());

            uti.registrarAdmissao(internacaoCom("Insuficiência respiratória aguda"));
            assertEquals(1, uti.getOcupacaoAtual());
        }

        @Test
        @DisplayName("Quarto em manutenção não recebe internações")
        void manutencaoBloqueiaAdmissao() {
            Quarto quarto = new Enfermaria(1L, "E-1", 1, 4, 0, StatusQuarto.DISPONIVEL);
            quarto.colocarEmManutencao();
            assertThrows(QuartoIndisponivelException.class, () -> quarto.registrarAdmissao(internacaoCom(null)));
            assertEquals(0, quarto.getLeitosDisponiveis());
        }
    }

    @Nested
    @DisplayName("Manutenção e capacidade")
    class ManutencaoECapacidade {

        @Test
        @DisplayName("Só quartos vazios entram em manutenção")
        void manutencaoSomenteVazio() {
            Quarto ocupado = new Enfermaria(1L, "E-1", 1, 4, 1, StatusQuarto.DISPONIVEL);
            assertThrows(RegraNegocioException.class, ocupado::colocarEmManutencao);

            Quarto vazio = new Enfermaria(2L, "E-2", 1, 4, 0, StatusQuarto.DISPONIVEL);
            vazio.colocarEmManutencao();
            assertEquals(StatusQuarto.EM_MANUTENCAO, vazio.getSituacao());
            assertThrows(RegraNegocioException.class, vazio::colocarEmManutencao);
        }

        @Test
        @DisplayName("Liberar da manutenção volta o quarto para DISPONIVEL")
        void liberarManutencao() {
            Quarto quarto = new Enfermaria(1L, "E-1", 1, 4, 0, StatusQuarto.EM_MANUTENCAO);
            quarto.liberarDaManutencao();
            assertEquals(StatusQuarto.DISPONIVEL, quarto.getSituacao());
            assertThrows(RegraNegocioException.class, quarto::liberarDaManutencao);
        }

        @Test
        @DisplayName("Capacidade não pode ficar abaixo da ocupação atual nem fora dos limites do tipo")
        void alterarCapacidade() {
            Quarto quarto = new Enfermaria(1L, "E-1", 1, 6, 3, StatusQuarto.DISPONIVEL);
            assertThrows(RegraNegocioException.class, () -> quarto.alterarCapacidade(2));
            assertThrows(RegraNegocioException.class, () -> quarto.alterarCapacidade(10));

            quarto.alterarCapacidade(3);
            assertEquals(3, quarto.getCapacidadeMaxima());
            assertEquals(StatusQuarto.OCUPADO, quarto.getSituacao());

            quarto.alterarCapacidade(5);
            assertEquals(StatusQuarto.DISPONIVEL, quarto.getSituacao());
        }
    }
}
