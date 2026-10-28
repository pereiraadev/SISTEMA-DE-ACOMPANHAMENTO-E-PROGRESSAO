package br.com.sistema.acompanhamento.api;

import br.com.sistema.acompanhamento.treino.domain.TreinoWorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api")
@Validated
public class FitTrackController {
    private final TreinoWorkflowService workflow;
    public FitTrackController(TreinoWorkflowService workflow) { this.workflow = workflow; }

    @GetMapping("/exercicios")
    public List<ApiDtos.ExercicioResposta> exercicios(Authentication auth) { return workflow.listarExercicios(auth.getName()); }
    @PostMapping("/exercicios")
    public ResponseEntity<ApiDtos.ExercicioResposta> criarExercicio(Authentication auth, @Valid @RequestBody ApiDtos.NovoExercicio entrada) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow.criarExercicio(auth.getName(), entrada));
    }
    @GetMapping("/exercicios/{id}")
    public ApiDtos.ExercicioResposta exercicio(Authentication auth, @PathVariable Long id) {
        return workflow.consultarExercicio(auth.getName(), id);
    }
    @GetMapping("/exercicios/{id}/maior-reps")
    public ApiDtos.MaiorRepsResposta maiorReps(Authentication auth, @PathVariable Long id,
            @RequestParam @DecimalMin("0.0") BigDecimal carga) {
        return new ApiDtos.MaiorRepsResposta(id, carga, workflow.maiorReps(auth.getName(), id, carga));
    }
    @GetMapping("/exercicios/{id}/recordes")
    public ApiDtos.RecordeResposta recordes(Authentication auth, @PathVariable Long id) {
        return workflow.recordes(auth.getName(), id);
    }
    @GetMapping("/exercicios/{id}/volume")
    public ApiDtos.VolumeExercicioResposta volumeExercicio(Authentication auth, @PathVariable Long id) {
        return workflow.volumeExercicio(auth.getName(), id);
    }
    @PostMapping("/modelos")
    public ResponseEntity<ApiDtos.TreinoResposta> criarModelo(Authentication auth, @Valid @RequestBody ApiDtos.NovoModelo entrada) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow.criarModelo(auth.getName(), entrada));
    }
    @GetMapping("/modelos")
    public List<ApiDtos.TreinoResposta> modelos(Authentication auth) { return workflow.modelos(auth.getName()); }
    @GetMapping("/modelos/{id}")
    public ApiDtos.TreinoResposta modelo(Authentication auth, @PathVariable Long id) { return workflow.modelo(auth.getName(), id); }
    @PostMapping("/modelos/{id}/iniciar")
    public ResponseEntity<ApiDtos.TreinoResposta> iniciar(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow.iniciar(auth.getName(), id));
    }
    @GetMapping("/execucoes/{id}")
    public ApiDtos.TreinoResposta execucao(Authentication auth, @PathVariable Long id) { return workflow.execucao(auth.getName(), id); }
    @PostMapping("/execucoes/{id}/itens/{itemId}/series")
    public ResponseEntity<ApiDtos.SerieResposta> registrar(Authentication auth, @PathVariable Long id,
            @PathVariable Long itemId, @Valid @RequestBody ApiDtos.SerieEntrada entrada) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow.registrarSerie(auth.getName(), id, itemId, entrada));
    }
    @PutMapping("/execucoes/{id}/itens/{itemId}/series/{serieId}")
    public ApiDtos.SerieResposta atualizar(Authentication auth, @PathVariable Long id, @PathVariable Long itemId,
            @PathVariable Long serieId, @Valid @RequestBody ApiDtos.SerieEntrada entrada) {
        return workflow.atualizarSerie(auth.getName(), id, itemId, serieId, entrada);
    }
    @PostMapping("/execucoes/{id}/finalizar")
    public ApiDtos.TreinoResposta finalizar(Authentication auth, @PathVariable Long id) { return workflow.finalizar(auth.getName(), id); }
    @GetMapping("/historico")
    public List<ApiDtos.TreinoResposta> historico(Authentication auth) { return workflow.historico(auth.getName()); }
    @GetMapping("/historico/{id}/anterior")
    public ApiDtos.TreinoResposta anterior(Authentication auth, @PathVariable Long id, @RequestParam Long exercicioId) {
        return workflow.anterior(auth.getName(), id, exercicioId);
    }
    @GetMapping("/historico/comparar")
    public ApiDtos.ComparacaoResposta comparar(Authentication auth, @RequestParam Long sessao1Id,
            @RequestParam Long sessao2Id, @RequestParam Long exercicioId) {
        return workflow.comparar(auth.getName(), sessao1Id, sessao2Id, exercicioId);
    }
    @GetMapping("/dashboard")
    public ApiDtos.DashboardResposta dashboard(Authentication auth) { return workflow.dashboard(auth.getName()); }
}
