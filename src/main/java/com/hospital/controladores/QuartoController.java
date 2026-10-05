package com.hospital.controladores;

import com.hospital.dtos.QuartoDTO;
import com.hospital.servicos.QuartoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService quartoService;

    public QuartoController(QuartoService quartoService) {
        this.quartoService = quartoService;
    }

    @PostMapping
    public ResponseEntity<QuartoDTO> cadastrarQuarto(@Valid @RequestBody QuartoDTO dto) {
        QuartoDTO salvo = quartoService.cadastrarQuarto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<QuartoDTO>> listarTodos() {
        return ResponseEntity.ok(quartoService.listarTodos());
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<QuartoDTO>> listarDisponiveis() {
        return ResponseEntity.ok(quartoService.listarDisponiveis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuartoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.buscarPorId(id));
    }
}
