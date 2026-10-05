package com.hospital.servicos;

import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfissionalSaudeService {

    private final ProfissionalSaudeRepository profissionalRepository;

    public ProfissionalSaudeService(ProfissionalSaudeRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional
    public ProfissionalSaudeDTO cadastrar(ProfissionalSaudeDTO dto) {
        if (profissionalRepository.existsByRegistroProfissional(dto.getRegistroProfissional())) {
            throw new RegraNegocioException("Já existe um profissional cadastrado com o registro: " + dto.getRegistroProfissional());
        }

        ProfissionalSaude profissional = new ProfissionalSaude();
        profissional.setNome(dto.getNome());
        profissional.setRegistroProfissional(dto.getRegistroProfissional());
        profissional.setEspecialidade(dto.getEspecialidade());
        profissional.setTelefone(dto.getTelefone());
        profissional.setEmail(dto.getEmail());

        ProfissionalSaude salvo = profissionalRepository.save(profissional);
        return new ProfissionalSaudeDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<ProfissionalSaudeDTO> listarTodos() {
        return profissionalRepository.findAll().stream()
                .map(ProfissionalSaudeDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfissionalSaudeDTO buscarPorId(Long id) {
        ProfissionalSaude profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional de saúde não encontrado com ID: " + id));
        return new ProfissionalSaudeDTO(profissional);
    }
}
