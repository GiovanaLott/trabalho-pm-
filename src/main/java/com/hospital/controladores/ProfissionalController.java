package com.hospital.controladores;

import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.servicos.ProfissionalSaudeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalSaudeService profissionalService;

    public ProfissionalController(ProfissionalSaudeService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @PostMapping
    public ResponseEntity<ProfissionalSaudeDTO> cadastrar(@Valid @RequestBody ProfissionalSaudeDTO dto) {
        ProfissionalSaudeDTO profissionalSalvo = profissionalService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(profissionalSalvo);
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalSaudeDTO>> listarTodos() {
        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalSaudeDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.buscarPorId(id));
    }

    @GetMapping("/especialidade/{especialidade}")
    public ResponseEntity<List<ProfissionalSaudeDTO>> buscarPorEspecialidade(
            @PathVariable String especialidade) {
        return ResponseEntity.ok(profissionalService.buscarPorEspecialidade(especialidade));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalSaudeDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody ProfissionalSaudeDTO dto) {
        return ResponseEntity.ok(profissionalService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        profissionalService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
