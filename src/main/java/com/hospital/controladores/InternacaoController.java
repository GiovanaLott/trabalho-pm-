package com.hospital.controladores;

import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.InternacaoRequestDTO;
import com.hospital.servicos.InternacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService internacaoService;

    public InternacaoController(InternacaoService internacaoService) {
        this.internacaoService = internacaoService;
    }

    @PostMapping
    public ResponseEntity<InternacaoDTO> realizarInternacao(@Valid @RequestBody InternacaoRequestDTO dto) {
        InternacaoDTO internacaoRealizada = internacaoService.realizarInternacao(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(internacaoRealizada);
    }

    @GetMapping
    public ResponseEntity<List<InternacaoDTO>> listarTodas() {
        return ResponseEntity.ok(internacaoService.listarTodas());
    }

    @GetMapping("/em-andamento")
    public ResponseEntity<List<InternacaoDTO>> listarEmAndamento() {
        return ResponseEntity.ok(internacaoService.listarEmAndamento());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InternacaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(internacaoService.buscarPorId(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<InternacaoDTO>> buscarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(internacaoService.buscarPorPaciente(pacienteId));
    }

    @PutMapping("/{id}/alta")
    public ResponseEntity<InternacaoDTO> darAlta(@PathVariable Long id) {
        return ResponseEntity.ok(internacaoService.darAlta(id));
    }
}
