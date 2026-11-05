package com.hospital.servicos;

import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.OcupacaoHospitalarDTO;
import com.hospital.dtos.QuartoDTO;
import com.hospital.dtos.QuartoUpdateDTO;
import com.hospital.entidades.Apartamento;
import com.hospital.entidades.Quarto;
import com.hospital.entidades.QuartoIsolamento;
import com.hospital.entidades.StatusInternacao;
import com.hospital.entidades.StatusQuarto;
import com.hospital.entidades.TipoIsolamento;
import com.hospital.entidades.TipoQuarto;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.QuartoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Regras de negócio de quartos.
 *
 * <ul>
 *   <li>Número de identificação único.</li>
 *   <li>Capacidade dentro da faixa permitida para o tipo do quarto.</li>
 *   <li>Ocupação e situação são controladas pelo sistema (entrada e alta de internações);
 *       não podem ser definidas manualmente no cadastro.</li>
 *   <li>Só quartos vazios entram em manutenção; quartos em manutenção não recebem internações.</li>
 *   <li>Quartos que já tiveram internações não podem ser excluídos (preservação do histórico).</li>
 * </ul>
 */
@Service
public class QuartoService {

    private final QuartoRepository quartoRepository;
    private final InternacaoRepository internacaoRepository;

    public QuartoService(QuartoRepository quartoRepository, InternacaoRepository internacaoRepository) {
        this.quartoRepository = quartoRepository;
        this.internacaoRepository = internacaoRepository;
    }

    @Transactional
    public QuartoDTO cadastrarQuarto(QuartoDTO dto) {
        String numero = dto.getNumeroIdentificacao() != null ? dto.getNumeroIdentificacao().trim() : null;
        if (numero != null && quartoRepository.existsByNumeroIdentificacao(numero)) {
            throw new RegraNegocioException("Já existe um quarto cadastrado com a identificação: " + numero);
        }

        TipoQuarto tipo = dto.getTipo() != null ? dto.getTipo() : TipoQuarto.ENFERMARIA;
        Quarto quarto = tipo.novoQuarto();
        quarto.setNumeroIdentificacao(numero);
        quarto.setAndar(dto.getAndar());
        quarto.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        quarto.setOcupacaoAtual(0);
        quarto.setSituacao(StatusQuarto.DISPONIVEL);
        aplicarAtributosDoTipo(quarto, dto.getPermiteAcompanhante(), dto.getTipoIsolamento());

        quarto.validarConfiguracao();
        return new QuartoDTO(quartoRepository.save(quarto));
    }

    /** Lista os quartos, com filtros opcionais por tipo, andar e situação. */
    @Transactional(readOnly = true)
    public List<QuartoDTO> listar(TipoQuarto tipo, Integer andar, StatusQuarto situacao) {
        Stream<Quarto> quartos = quartoRepository.findAllByOrderByAndarAscNumeroIdentificacaoAsc().stream();
        if (tipo != null) {
            quartos = quartos.filter(q -> q.getTipo() == tipo);
        }
        if (andar != null) {
            quartos = quartos.filter(q -> andar.equals(q.getAndar()));
        }
        if (situacao != null) {
            quartos = quartos.filter(q -> q.getSituacao() == situacao);
        }
        return quartos.map(QuartoDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<QuartoDTO> listarTodos() {
        return listar(null, null, null);
    }

    /** Quartos com pelo menos um leito livre (opcionalmente de um tipo específico). */
    @Transactional(readOnly = true)
    public List<QuartoDTO> listarDisponiveis(TipoQuarto tipo) {
        return quartoRepository.findBySituacao(StatusQuarto.DISPONIVEL).stream()
                .filter(Quarto::temVagaDisponivel)
                .filter(q -> tipo == null || q.getTipo() == tipo)
                .map(QuartoDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuartoDTO> listarDisponiveis() {
        return listarDisponiveis(null);
    }

    @Transactional(readOnly = true)
    public QuartoDTO buscarPorId(Long id) {
        return new QuartoDTO(buscarEntidade(id));
    }

    @Transactional
    public QuartoDTO atualizar(Long id, QuartoUpdateDTO dto) {
        Quarto quarto = buscarEntidade(id);

        if (dto.getNumeroIdentificacao() != null) {
            String numero = dto.getNumeroIdentificacao().trim();
            if (quartoRepository.existsByNumeroIdentificacaoAndIdNot(numero, id)) {
                throw new RegraNegocioException("Já existe outro quarto cadastrado com a identificação: " + numero);
            }
            quarto.setNumeroIdentificacao(numero);
        }
        if (dto.getAndar() != null) {
            quarto.setAndar(dto.getAndar());
        }
        aplicarAtributosDoTipo(quarto, dto.getPermiteAcompanhante(), dto.getTipoIsolamento());
        quarto.alterarCapacidade(dto.getCapacidadeMaxima());

        quarto.validarConfiguracao();
        return new QuartoDTO(quartoRepository.save(quarto));
    }

    @Transactional
    public QuartoDTO colocarEmManutencao(Long id) {
        Quarto quarto = buscarEntidade(id);
        quarto.colocarEmManutencao();
        return new QuartoDTO(quartoRepository.save(quarto));
    }

    @Transactional
    public QuartoDTO liberarDaManutencao(Long id) {
        Quarto quarto = buscarEntidade(id);
        quarto.liberarDaManutencao();
        return new QuartoDTO(quartoRepository.save(quarto));
    }

    @Transactional
    public void excluir(Long id) {
        Quarto quarto = buscarEntidade(id);
        if (internacaoRepository.existsByQuartoId(id)) {
            throw new RegraNegocioException("O quarto " + quarto.getNumeroIdentificacao()
                    + " possui internações registradas e não pode ser excluído, pois o histórico deve ser preservado. "
                    + "Para retirá-lo de uso, coloque-o em manutenção.");
        }
        quartoRepository.delete(quarto);
    }

    /** Internações em andamento no quarto (quem está ocupando os leitos). */
    @Transactional(readOnly = true)
    public List<InternacaoDTO> listarInternacoesAtivas(Long id) {
        buscarEntidade(id);
        return internacaoRepository.findByQuartoIdAndStatus(id, StatusInternacao.EM_ANDAMENTO).stream()
                .map(InternacaoDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public OcupacaoHospitalarDTO resumoOcupacao() {
        List<Quarto> quartos = quartoRepository.findAll();

        List<OcupacaoHospitalarDTO.PorTipo> porTipo = Arrays.stream(TipoQuarto.values())
                .map(tipo -> {
                    List<Quarto> doTipo = quartos.stream().filter(q -> q.getTipo() == tipo).toList();
                    return new OcupacaoHospitalarDTO.PorTipo(tipo, tipo.getDescricao(), doTipo.size(),
                            somarCapacidade(doTipo), somarOcupacao(doTipo), somarLeitosDisponiveis(doTipo));
                })
                .toList();

        int totalLeitos = somarCapacidade(quartos);
        int ocupados = somarOcupacao(quartos);
        int emManutencao = (int) quartos.stream().filter(q -> q.getSituacao() == StatusQuarto.EM_MANUTENCAO).count();
        double taxa = totalLeitos == 0 ? 0.0 : Math.round(ocupados * 1000.0 / totalLeitos) / 10.0;

        return new OcupacaoHospitalarDTO(quartos.size(), emManutencao, totalLeitos, ocupados,
                somarLeitosDisponiveis(quartos), taxa, porTipo);
    }

    private void aplicarAtributosDoTipo(Quarto quarto, Boolean permiteAcompanhante,
                                        TipoIsolamento tipoIsolamento) {
        if (quarto instanceof Apartamento apartamento && permiteAcompanhante != null) {
            apartamento.setPermiteAcompanhante(permiteAcompanhante);
        }
        if (quarto instanceof QuartoIsolamento isolamento && tipoIsolamento != null) {
            isolamento.setTipoIsolamento(tipoIsolamento);
        }
    }

    private Quarto buscarEntidade(Long id) {
        return quartoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto não encontrado com ID: " + id));
    }

    private static int somarCapacidade(List<Quarto> quartos) {
        return quartos.stream().mapToInt(Quarto::getCapacidadeMaxima).sum();
    }

    private static int somarOcupacao(List<Quarto> quartos) {
        return quartos.stream().mapToInt(Quarto::getOcupacaoAtual).sum();
    }

    private static int somarLeitosDisponiveis(List<Quarto> quartos) {
        return quartos.stream().mapToInt(Quarto::getLeitosDisponiveis).sum();
    }
}
