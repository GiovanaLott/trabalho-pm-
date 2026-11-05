package com.hospital.controladores;

import com.hospital.dtos.PacienteDTO;
import com.hospital.servicos.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<PacienteDTO> cadastrar(@Valid @RequestBody PacienteDTO dto) {
        PacienteDTO pacienteSalvo = pacienteService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteSalvo);
    }

    /** Lista todos os pacientes em ordem alfabética, ou filtra por parte do nome ({@code ?nome=}). */
    @GetMapping
    public ResponseEntity<List<PacienteDTO>> listar(@RequestParam(required = false) String nome) {
        return ResponseEntity.ok(pacienteService.buscarPorNome(nome));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    /** Aceita o CPF com ou sem máscara (ex.: 52998224725 ou 529.982.247-25). */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<PacienteDTO> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(pacienteService.buscarPorCpf(cpf));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        return ResponseEntity.ok(pacienteService.atualizar(id, dto));
    }

    /** Só é permitido para pacientes sem histórico (consultas, internações ou cirurgias). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pacienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
