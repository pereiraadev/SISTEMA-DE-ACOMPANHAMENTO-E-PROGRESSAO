package com.fittrack.fittrack_frontend.client;

import java.util.List;

import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;
import com.fittrack.fittrack_frontend.dto.CadastroDTO;
import com.fittrack.fittrack_frontend.dto.DesempenhoGrupoDTO;
import com.fittrack.fittrack_frontend.dto.DetalheGrupoDTO;
import com.fittrack.fittrack_frontend.dto.EstatisticasGeraisDTO;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.HistoricoExercicioItemDTO;
import com.fittrack.fittrack_frontend.dto.LoginDTO;
import com.fittrack.fittrack_frontend.dto.RecordesPessoaisDTO;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP das operações do praticante, conectando as telas à API Spring Boot.
 * As chamadas são bloqueantes; controllers devem executá-las por meio de {@link com.fittrack.fittrack_frontend.util.FxAsync}
 * para manter livre a JavaFX Application Thread e apresentar falhas por feedback visual.
 */
@Component
public class PraticanteApiClient {

    /** Cliente configurado com URL-base, serialização JSON e autenticação da sessão. */
    private final RestClient restClient;

    /**
     * Recebe o cliente HTTP configurado pelo contexto Spring.
     *
     * @param fitTrackRestClient cliente reutilizado nas operações de praticante
     */
    public PraticanteApiClient(RestClient fitTrackRestClient) {
        this.restClient = fitTrackRestClient;
    }

    /**
     * Envia credenciais ao endpoint de login.
     *
     * @param dto e-mail e senha preenchidos na tela
     * @return dados da sessão autenticada
     */
    public AuthResponseDTO login(LoginDTO dto) {
        return ApiException.chamar(() -> restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(AuthResponseDTO.class));
    }

    /**
     * Cria uma conta e recebe os dados de sessão retornados pela API.
     *
     * @param dto nome, e-mail e senha do cadastro
     * @return dados do praticante autenticado após o cadastro
     */
    public AuthResponseDTO cadastrar(CadastroDTO dto) {
        return ApiException.chamar(() -> restClient.post()
                .uri("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(AuthResponseDTO.class));
    }

    /**
     * Consulta os indicadores gerais exibidos no dashboard.
     *
     * @param praticanteId identificador do praticante
     * @return estatísticas consolidadas
     */
    public EstatisticasGeraisDTO buscarEstatisticasGerais(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/estatisticas", praticanteId)
                .retrieve()
                .body(EstatisticasGeraisDTO.class));
    }

    /**
     * Busca exercícios ordenados pela recorrência para os destaques da tela inicial.
     *
     * @param praticanteId identificador do praticante
     * @return exercícios mais realizados
     */
    public List<ExercicioDTO> listarExerciciosMaisRealizados(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/exercicios/mais-realizados", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExercicioDTO>>() {}));
    }

    /**
     * Carrega o catálogo usado na aba de exercícios e nos filtros de métricas.
     *
     * @param praticanteId identificador do praticante
     * @return exercícios disponíveis no catálogo
     */
    public List<ExercicioDTO> listarExercicios(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/exercicios", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExercicioDTO>>() {}));
    }

    /**
     * Obtém as execuções anteriores para compor histórico e gráfico de evolução.
     *
     * @param praticanteId identificador do praticante
     * @param exercicioId identificador do exercício selecionado
     * @return registros históricos do exercício
     */
    public List<HistoricoExercicioItemDTO> listarHistoricoExercicio(Long praticanteId, Long exercicioId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/exercicios/{exercicioId}/historico", praticanteId, exercicioId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<HistoricoExercicioItemDTO>>() {}));
    }

    /**
     * Obtém os indicadores de desempenho de cada grupo muscular.
     *
     * @param praticanteId identificador do praticante
     * @return lista de métricas por grupo
     */
    public List<DesempenhoGrupoDTO> listarDesempenhoGrupos(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/metricas/grupos", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<DesempenhoGrupoDTO>>() {}));
    }

    /**
     * Carrega volumes por exercício e frequência mensal para um grupo muscular.
     *
     * @param praticanteId identificador do praticante
     * @param grupoMuscular código do grupo solicitado
     * @return dados detalhados do grupo
     */
    public DetalheGrupoDTO buscarDetalheGrupo(Long praticanteId, String grupoMuscular) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/metricas/grupos/{grupo}", praticanteId, grupoMuscular)
                .retrieve()
                .body(DetalheGrupoDTO.class));
    }

    /**
     * Consulta recordes agregados e recordes individuais para a tela Hall da Fama.
     *
     * @param praticanteId identificador do praticante
     * @return recordes pessoais do praticante
     */
    public RecordesPessoaisDTO buscarRecordesPessoais(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/recordes", praticanteId)
                .retrieve()
                .body(RecordesPessoaisDTO.class));
    }
}
