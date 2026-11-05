package com.hospital.servicos;

import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.entidades.*;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfissionalSaudeService {

    private final ProfissionalSaudeRepository profissionalRepository;
    private final ConsultaRepository consultaRepository;
    private final InternacaoRepository internacaoRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public ProfissionalSaudeService(ProfissionalSaudeRepository profissionalRepository,
                                    ConsultaRepository consultaRepository,
                                    InternacaoRepository internacaoRepository,
                                    ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.profissionalRepository = profissionalRepository;
        this.consultaRepository = consultaRepository;
        this.internacaoRepository = internacaoRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional
    public ProfissionalSaudeDTO cadastrar(ProfissionalSaudeDTO dto) {
        if (profissionalRepository.existsByRegistroProfissional(dto.getRegistroProfissional())) {
            throw new RegraNegocioException("Já existe um profissional cadastrado com o registro: " + dto.getRegistroProfissional());
        }

        ProfissionalSaude profissional = criarPorCategoria(dto);
        profissional.setNome(dto.getNome());
        profissional.setRegistroProfissional(dto.getRegistroProfissional());
        profissional.setEspecialidade(dto.getEspecialidade());
        profissional.setTelefone(dto.getTelefone());
        profissional.setEmail(dto.getEmail());

        ProfissionalSaude salvo = profissionalRepository.save(profissional);
        return new ProfissionalSaudeDTO(salvo);
    }

    private ProfissionalSaude criarPorCategoria(ProfissionalSaudeDTO dto) {
        if (dto.getCategoria() == null) {
            throw new RegraNegocioException("A categoria profissional é obrigatória.");
        }

        if (dto.getCategoria() == CategoriaProfissional.ESPECIALISTA
                && (dto.getEspecialidade() == null || dto.getEspecialidade().isBlank())) {
            throw new RegraNegocioException("A especialidade é obrigatória para profissionais especialistas.");
        }

        return switch (dto.getCategoria()) {
            case CLINICO_GERAL -> new ClinicoGeral();
            case ESPECIALISTA -> new Especialista();
            case CIRURGIAO -> new Cirurgiao();
        };
    }

    @Transactional(readOnly = true)
    public List<ProfissionalSaudeDTO> listarTodos() {
        return profissionalRepository.findAll().stream()
                .map(ProfissionalSaudeDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfissionalSaudeDTO buscarPorId(Long id) {
        ProfissionalSaude profissional = buscarEntidade(id);
        return new ProfissionalSaudeDTO(profissional);
    }

    @Transactional(readOnly = true)
    public List<ProfissionalSaudeDTO> buscarPorEspecialidade(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            throw new RegraNegocioException("Informe uma especialidade para realizar a pesquisa.");
        }
        return profissionalRepository
                .findEspecialistasByEspecialidadeContainingIgnoreCase(especialidade.trim()).stream()
                .map(ProfissionalSaudeDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfissionalSaudeDTO atualizar(Long id, ProfissionalSaudeDTO dto) {
        ProfissionalSaude profissional = buscarEntidade(id);
        CategoriaProfissional categoriaAtual = new ProfissionalSaudeDTO(profissional).getCategoria();
        if (dto.getCategoria() != null && dto.getCategoria() != categoriaAtual) {
            throw new RegraNegocioException("A categoria profissional não pode ser alterada após o cadastro.");
        }
        if (profissionalRepository.existsByRegistroProfissionalAndIdNot(dto.getRegistroProfissional(), id)) {
            throw new RegraNegocioException("Já existe outro profissional com o registro: " + dto.getRegistroProfissional());
        }
        if (profissional instanceof Especialista
                && (dto.getEspecialidade() == null || dto.getEspecialidade().isBlank())) {
            throw new RegraNegocioException("A especialidade é obrigatória para profissionais especialistas.");
        }

        profissional.setNome(dto.getNome());
        profissional.setRegistroProfissional(dto.getRegistroProfissional());
        profissional.setEspecialidade(dto.getEspecialidade());
        profissional.setTelefone(dto.getTelefone());
        profissional.setEmail(dto.getEmail());
        return new ProfissionalSaudeDTO(profissionalRepository.save(profissional));
    }

    @Transactional
    public void excluir(Long id) {
        buscarEntidade(id);
        if (consultaRepository.existsByProfissionalId(id)
                || internacaoRepository.existsByProfissionalId(id)
                || procedimentoRepository.existsByCirurgiaoId(id)) {
            throw new RegraNegocioException(
                    "O profissional possui atendimentos no histórico e não pode ser excluído.");
        }
        profissionalRepository.deleteById(id);
    }

    private ProfissionalSaude buscarEntidade(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional de saúde não encontrado com ID: " + id));
    }
}
