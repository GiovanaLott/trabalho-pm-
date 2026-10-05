package com.hospital.controladores;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.servicos.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    public ResponseEntity<ConsultaDTO> agendarConsulta(@Valid @RequestBody ConsultaRequestDTO dto) {
        ConsultaDTO consultaAgendada = consultaService.agendarConsulta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaAgendada);
    }

    @GetMapping
    public ResponseEntity<List<ConsultaDTO>> listarTodas() {
        return ResponseEntity.ok(consultaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaDTO>> buscarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(consultaService.buscarPorPaciente(pacienteId));
    }

    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<ConsultaDTO>> buscarPorProfissional(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(consultaService.buscarPorProfissional(profissionalId));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaDTO> cancelarConsulta(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.cancelarConsulta(id));
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<ConsultaDTO> finalizarConsulta(@PathVariable Long id, @RequestParam(required = false) String observacoesMedicas) {
        return ResponseEntity.ok(consultaService.finalizarConsulta(id, observacoesMedicas));
    }
}
