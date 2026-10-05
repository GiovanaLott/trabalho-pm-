package com.hospital.servicos;

import com.hospital.dtos.QuartoDTO;
import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.QuartoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuartoService {

    private final QuartoRepository quartoRepository;

    public QuartoService(QuartoRepository quartoRepository) {
        this.quartoRepository = quartoRepository;
    }

    @Transactional
    public QuartoDTO cadastrarQuarto(QuartoDTO dto) {
        if (quartoRepository.existsByNumeroIdentificacao(dto.getNumeroIdentificacao())) {
            throw new RegraNegocioException("Já existe um quarto cadastrado com a identificação: " + dto.getNumeroIdentificacao());
        }

        Quarto quarto = new Quarto();
        quarto.setNumeroIdentificacao(dto.getNumeroIdentificacao());
        quarto.setAndar(dto.getAndar());
        quarto.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        quarto.setOcupacaoAtual(0);
        quarto.setSituacao(StatusQuarto.DISPONIVEL);

        Quarto salvo = quartoRepository.save(quarto);
        return new QuartoDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<QuartoDTO> listarTodos() {
        return quartoRepository.findAll().stream()
                .map(QuartoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuartoDTO> listarDisponiveis() {
        return quartoRepository.findBySituacao(StatusQuarto.DISPONIVEL).stream()
                .map(QuartoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuartoDTO buscarPorId(Long id) {
        Quarto quarto = quartoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto não encontrado com ID: " + id));
        return new QuartoDTO(quarto);
    }
}
