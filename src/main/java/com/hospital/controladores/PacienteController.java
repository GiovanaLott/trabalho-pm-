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

    @GetMapping
    public ResponseEntity<List<PacienteDTO>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }
}
