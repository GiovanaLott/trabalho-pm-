package com.hospital.controladores;

import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.OcupacaoHospitalarDTO;
import com.hospital.dtos.QuartoDTO;
import com.hospital.dtos.QuartoUpdateDTO;
import com.hospital.entidades.StatusQuarto;
import com.hospital.entidades.TipoQuarto;
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

    /** Lista os quartos. Filtros opcionais: {@code ?tipo=UTI&andar=2&situacao=DISPONIVEL}. */
    @GetMapping
    public ResponseEntity<List<QuartoDTO>> listar(@RequestParam(required = false) TipoQuarto tipo,
                                                  @RequestParam(required = false) Integer andar,
                                                  @RequestParam(required = false) StatusQuarto situacao) {
        return ResponseEntity.ok(quartoService.listar(tipo, andar, situacao));
    }

    /** Quartos com leito livre. Filtro opcional: {@code ?tipo=ENFERMARIA}. */
    @GetMapping("/disponiveis")
    public ResponseEntity<List<QuartoDTO>> listarDisponiveis(@RequestParam(required = false) TipoQuarto tipo) {
        return ResponseEntity.ok(quartoService.listarDisponiveis(tipo));
    }

    /** Painel de ocupação: totais de leitos, ocupados, disponíveis e taxa de ocupação por tipo. */
    @GetMapping("/ocupacao")
    public ResponseEntity<OcupacaoHospitalarDTO> resumoOcupacao() {
        return ResponseEntity.ok(quartoService.resumoOcupacao());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuartoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.buscarPorId(id));
    }

    /** Pacientes internados no quarto neste momento. */
    @GetMapping("/{id}/internacoes")
    public ResponseEntity<List<InternacaoDTO>> listarInternacoesAtivas(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.listarInternacoesAtivas(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuartoDTO> atualizar(@PathVariable Long id, @Valid @RequestBody QuartoUpdateDTO dto) {
        return ResponseEntity.ok(quartoService.atualizar(id, dto));
    }

    @PutMapping("/{id}/manutencao")
    public ResponseEntity<QuartoDTO> colocarEmManutencao(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.colocarEmManutencao(id));
    }

    @PutMapping("/{id}/liberar")
    public ResponseEntity<QuartoDTO> liberarDaManutencao(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.liberarDaManutencao(id));
    }

    /** Só é permitido para quartos que nunca tiveram internações. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        quartoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
