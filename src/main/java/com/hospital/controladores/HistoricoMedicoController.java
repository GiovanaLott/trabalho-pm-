package com.hospital.controladores;

import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.servicos.HistoricoMedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/historico")
public class HistoricoMedicoController {

    private final HistoricoMedicoService historicoMedicoService;

    public HistoricoMedicoController(HistoricoMedicoService historicoMedicoService) {
        this.historicoMedicoService = historicoMedicoService;
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<HistoricoMedicoDTO> buscarHistoricoPorPaciente(@PathVariable Long pacienteId) {
        HistoricoMedicoDTO historico = historicoMedicoService.buscarHistoricoPorPaciente(pacienteId);
        return ResponseEntity.ok(historico);
    }
}
