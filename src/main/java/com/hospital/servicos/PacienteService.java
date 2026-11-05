package com.hospital.servicos;

import com.hospital.dtos.PacienteDTO;
import com.hospital.entidades.Cpf;
import com.hospital.entidades.Paciente;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negócio de pacientes.
 *
 * <ul>
 *   <li>CPF obrigatório, válido (dígitos verificadores) e único; armazenado sempre com máscara.</li>
 *   <li>Data de nascimento no passado e plausível (até 130 anos).</li>
 *   <li>Um paciente com consultas, internações ou cirurgias registradas não pode ser excluído,
 *       para preservar o histórico médico (regra de negócio 6 do enunciado).</li>
 * </ul>
 */
@Service
public class PacienteService {

    private static final int IDADE_MAXIMA = 130;

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final InternacaoRepository internacaoRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public PacienteService(PacienteRepository pacienteRepository,
                           ConsultaRepository consultaRepository,
                           InternacaoRepository internacaoRepository,
                           ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.internacaoRepository = internacaoRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional
    public PacienteDTO cadastrar(PacienteDTO dto) {
        String cpf = normalizarCpf(dto.getCpf());
        if (pacienteRepository.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe um paciente cadastrado com o CPF: " + cpf);
        }
        validarDataNascimento(dto.getDataNascimento());

        Paciente paciente = new Paciente();
        paciente.setCpf(cpf);
        paciente.atualizarDados(limpar(dto.getNome()), dto.getDataNascimento(),
                limpar(dto.getTelefone()), limpar(dto.getEndereco()), limpar(dto.getEmail()));

        return new PacienteDTO(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public List<PacienteDTO> listarTodos() {
        return pacienteRepository.findAllByOrderByNomeAsc().stream()
                .map(PacienteDTO::new)
                .toList();
    }

    /** Busca por parte do nome, sem diferenciar maiúsculas/minúsculas. Sem filtro, lista todos. */
    @Transactional(readOnly = true)
    public List<PacienteDTO> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return listarTodos();
        }
        return pacienteRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.trim()).stream()
                .map(PacienteDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PacienteDTO buscarPorId(Long id) {
        return new PacienteDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PacienteDTO buscarPorCpf(String cpf) {
        String cpfNormalizado = normalizarCpf(cpf);
        return pacienteRepository.findByCpf(cpfNormalizado)
                .map(PacienteDTO::new)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com CPF: " + cpfNormalizado));
    }

    @Transactional
    public PacienteDTO atualizar(Long id, PacienteDTO dto) {
        Paciente paciente = buscarEntidade(id);

        String cpf = normalizarCpf(dto.getCpf());
        if (pacienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new RegraNegocioException("Já existe outro paciente cadastrado com o CPF: " + cpf);
        }
        validarDataNascimento(dto.getDataNascimento());

        paciente.setCpf(cpf);
        paciente.atualizarDados(limpar(dto.getNome()), dto.getDataNascimento(),
                limpar(dto.getTelefone()), limpar(dto.getEndereco()), limpar(dto.getEmail()));

        return new PacienteDTO(pacienteRepository.save(paciente));
    }

    @Transactional
    public void excluir(Long id) {
        Paciente paciente = buscarEntidade(id);
        if (possuiHistorico(id)) {
            throw new RegraNegocioException("O paciente " + paciente.getNome()
                    + " possui consultas, internações ou cirurgias registradas e não pode ser excluído, "
                    + "pois o histórico médico deve ser preservado.");
        }
        pacienteRepository.delete(paciente);
    }

    private boolean possuiHistorico(Long pacienteId) {
        return consultaRepository.existsByPacienteId(pacienteId)
                || internacaoRepository.existsByPacienteId(pacienteId)
                || procedimentoRepository.existsByPacienteId(pacienteId);
    }

    private Paciente buscarEntidade(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + id));
    }

    private String normalizarCpf(String cpf) {
        if (!Cpf.isValido(cpf)) {
            throw new RegraNegocioException("CPF inválido: " + cpf);
        }
        return Cpf.formatar(cpf);
    }

    private void validarDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new RegraNegocioException("A data de nascimento é obrigatória.");
        }
        if (!dataNascimento.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de nascimento deve estar no passado.");
        }
        if (dataNascimento.isBefore(LocalDate.now().minusYears(IDADE_MAXIMA))) {
            throw new RegraNegocioException("Data de nascimento inválida: idade superior a " + IDADE_MAXIMA + " anos.");
        }
    }

    private String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}
