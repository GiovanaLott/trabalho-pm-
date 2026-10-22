package com.hospital.controladores;

import com.hospital.dtos.ConclusaoCirurgiaDTO;
import com.hospital.dtos.ProcedimentoCirurgicoDTO;
import com.hospital.dtos.ProcedimentoCirurgicoRequestDTO;
import com.hospital.servicos.ProcedimentoCirurgicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/procedimentos")
public class ProcedimentoCirurgicoController {

    private final ProcedimentoCirurgicoService procedimentoService;

    public ProcedimentoCirurgicoController(ProcedimentoCirurgicoService procedimentoService) {
        this.procedimentoService = procedimentoService;
    }

    @PostMapping
    public ResponseEntity<ProcedimentoCirurgicoDTO> agendar(@Valid @RequestBody ProcedimentoCirurgicoRequestDTO dto) {
        ProcedimentoCirurgicoDTO agendado = procedimentoService.agendar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(agendado);
    }

    @GetMapping
    public ResponseEntity<List<ProcedimentoCirurgicoDTO>> listarTodos() {
        return ResponseEntity.ok(procedimentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedimentoCirurgicoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(procedimentoService.buscarPorId(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ProcedimentoCirurgicoDTO>> buscarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(procedimentoService.buscarPorPaciente(pacienteId));
    }

    @GetMapping("/cirurgiao/{cirurgiaoId}")
    public ResponseEntity<List<ProcedimentoCirurgicoDTO>> buscarPorCirurgiao(@PathVariable Long cirurgiaoId) {
        return ResponseEntity.ok(procedimentoService.buscarPorCirurgiao(cirurgiaoId));
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<ProcedimentoCirurgicoDTO> iniciarCirurgia(@PathVariable Long id) {
        return ResponseEntity.ok(procedimentoService.iniciarCirurgia(id));
    }

    @PutMapping("/{id}/concluir")
    public ResponseEntity<ProcedimentoCirurgicoDTO> concluirCirurgia(@PathVariable Long id,
                                                                    @RequestBody(required = false) ConclusaoCirurgiaDTO dto) {
        return ResponseEntity.ok(procedimentoService.concluirCirurgia(id, dto));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ProcedimentoCirurgicoDTO> cancelarCirurgia(@PathVariable Long id) {
        return ResponseEntity.ok(procedimentoService.cancelarCirurgia(id));
    }
}
