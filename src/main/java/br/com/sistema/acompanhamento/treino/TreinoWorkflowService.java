package br.com.sistema.acompanhamento.treino.domain;

import br.com.sistema.acompanhamento.api.*;
import br.com.sistema.acompanhamento.exercicio.domain.*;
import br.com.sistema.acompanhamento.praticante.domain.*;
import br.com.sistema.acompanhamento.historico.domain.*;
import br.com.sistema.acompanhamento.comparativo.domain.*;
import br.com.sistema.acompanhamento.praticante.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.treino.repository.TreinoRepository;
import br.com.sistema.acompanhamento.treino.repository.SerieExecutadaRepository;
import br.com.sistema.acompanhamento.treino.service.TreinoService;
import br.com.sistema.acompanhamento.historico.structure.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreinoWorkflowService {
    private final PraticanteRepository praticantes;
    private final TreinoRepository treinos;
    private final SerieExecutadaRepository series;
    private final ExercicioRepository exercicios;
    private final TreinoService clonador;

    public TreinoWorkflowService(PraticanteRepository praticantes, TreinoRepository treinos,
            SerieExecutadaRepository series, ExercicioRepository exercicios, TreinoService clonador) {
        this.praticantes = praticantes; this.treinos = treinos; this.series = series;
        this.exercicios = exercicios; this.clonador = clonador;
    }

    private ApiException naoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Recurso não encontrado");
    }
    private ApiException conflito(String mensagem) {
        return new ApiException(HttpStatus.CONFLICT, "ESTADO_INVALIDO", mensagem);
    }
    private Praticante dono(String email) {
        return praticantes.findByEmailIgnoreCase(email).orElseThrow(this::naoEncontrado);
    }
    private Treino treino(Long id, Long donoId) {
        Treino t = treinos.findById(id).orElseThrow(this::naoEncontrado);
        if (!Objects.equals(t.getPraticante().getId(), donoId)) throw naoEncontrado();
        return t;
    }
    private Treino treinoBloqueado(Long id, Long donoId) {
        Treino t = treinos.findLockedById(id).orElseThrow(this::naoEncontrado);
        if (!Objects.equals(t.getPraticante().getId(), donoId)) throw naoEncontrado();
        return t;
    }
    private Exercicio exercicio(Long id, Long donoId) {
        Exercicio e = exercicios.findById(id).orElseThrow(this::naoEncontrado);
        if (e.getPraticante() != null && !Objects.equals(e.getPraticante().getId(), donoId))
            throw naoEncontrado();
        return e;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ExercicioResposta> listarExercicios(String email) {
        Long id = dono(email).getId();
        return exercicios.disponiveis(id)
                .stream().map(this::exercicioResposta).toList();
    }
    private ApiDtos.ExercicioResposta exercicioResposta(Exercicio e) {
        boolean proprio = e.getPraticante() != null;
        return new ApiDtos.ExercicioResposta(e.getId(), e.getNome(), e.getGrupoMuscular(), e.getDescricao(), e.isAtivo(),
                proprio ? e.getRecordeCarga() : null, proprio ? e.getRecordeRepsParaCarga() : null,
                proprio ? e.getRecordeVolumeSessao() : null, proprio ? e.getTotalVezesRealizado() : 0);
    }
    @Transactional
    public ApiDtos.ExercicioResposta criarExercicio(String email, ApiDtos.NovoExercicio entrada) {
        GrupoMuscularEnum grupo;
        try { grupo = GrupoMuscularEnum.valueOf(entrada.grupoMuscular().trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException erro) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GRUPO_INVALIDO", "Grupo muscular inválido");
        }
        Exercicio e = new Exercicio();
        e.setPraticante(dono(email)); e.setNome(entrada.nome().trim()); e.setGrupoMuscular(grupo.name());
        e.setDescricao(entrada.descricao());
        return exercicioResposta(exercicios.saveAndFlush(e));
    }
    @Transactional(readOnly = true)
    public ApiDtos.ExercicioResposta consultarExercicio(String email, Long id) {
        return exercicioResposta(exercicio(id, dono(email).getId()));
    }

    @Transactional
    public ApiDtos.TreinoResposta criarModelo(String email, ApiDtos.NovoModelo entrada) {
        Praticante p = dono(email);
        Treino modelo = new Treino();
        modelo.setPraticante(p);
        modelo.setIdentificacao(entrada.identificacao().trim());
        modelo.setModelo(true);
        modelo.setStatus("MODELO");
        Set<Integer> ordens = new HashSet<>();
        for (ApiDtos.ItemModelo dado : entrada.itens()) {
            if (!ordens.add(dado.ordem())) throw conflito("Ordem repetida no modelo");
            ItemTreino item = new ItemTreino();
            Exercicio escolhido = exercicio(dado.exercicioId(), p.getId());
            if (!escolhido.isAtivo()) throw naoEncontrado();
            item.setExercicio(escolhido);
            item.setOrdem(dado.ordem()); item.setSeriesPrevistas(dado.seriesPrevistas());
            item.setRepsPrevistas(dado.repsPrevistas()); item.setCargaPrevista(dado.cargaPrevista());
            modelo.adicionarItem(item);
        }
        return resposta(treinos.saveAndFlush(modelo), Map.of());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.TreinoResposta> modelos(String email) {
        return treinos.findDistinctByPraticanteIdAndModeloTrueOrderByIdDesc(dono(email).getId())
                .stream().map(t -> resposta(t, Map.of())).toList();
    }
    @Transactional(readOnly = true)
    public ApiDtos.TreinoResposta modelo(String email, Long id) {
        Treino t = treino(id, dono(email).getId());
        if (!t.isModelo()) throw naoEncontrado();
        return resposta(t, Map.of());
    }
    @Transactional
    public ApiDtos.TreinoResposta iniciar(String email, Long modeloId) {
        Treino modelo = treino(modeloId, dono(email).getId());
        if (!modelo.isModelo()) throw conflito("Treino informado não é modelo");
        Treino execucao = clonador.clonarParaExecucao(modelo);
        execucao.setIniciadoEm(OffsetDateTime.now());
        execucao.setDataTreino(execucao.getIniciadoEm().toLocalDate());
        treinos.flush();
        return resposta(execucao, Map.of());
    }
    @Transactional(readOnly = true)
    public ApiDtos.TreinoResposta execucao(String email, Long id) {
        Treino t = treino(id, dono(email).getId());
        if (t.isModelo()) throw naoEncontrado();
        return resposta(t, seriesPorItem(List.of(t.getId())));
    }

    @Transactional
    public ApiDtos.SerieResposta registrarSerie(String email, Long treinoId, Long itemId, ApiDtos.SerieEntrada entrada) {
        Treino t = treinoBloqueado(treinoId, dono(email).getId());
        if (t.isModelo() || !"EM_ANDAMENTO".equals(t.getStatus())) throw conflito("Execução não está em andamento");
        ItemTreino item = t.getItens().stream().filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst().orElseThrow(this::naoEncontrado);
        if (entrada.numeroSerie() > item.getSeriesPrevistas()) throw conflito("Número de série excede o planejado");
        if (series.existsByItemTreinoIdAndNumeroSerie(itemId, entrada.numeroSerie()))
            throw conflito("Número de série já registrado");
        if (entrada.numeroSerie() > 1 && !series.existsByItemTreinoIdAndNumeroSerie(itemId, entrada.numeroSerie() - 1))
            throw conflito("Registre as séries na ordem");
        SerieExecutada serie = new SerieExecutada();
        serie.setItemTreino(item); serie.setNumeroSerie(entrada.numeroSerie());
        serie.setCargaUtilizada(entrada.carga()); serie.setRepeticoesRealizadas(entrada.repeticoes());
        serie.setHorarioConclusao(OffsetDateTime.now());
        series.saveAndFlush(serie);
        return serieResposta(serie);
    }
    @Transactional
    public ApiDtos.SerieResposta atualizarSerie(String email, Long treinoId, Long itemId, Long serieId,
                                                 ApiDtos.SerieEntrada entrada) {
        Treino t = treinoBloqueado(treinoId, dono(email).getId());
        if (t.isModelo() || !"EM_ANDAMENTO".equals(t.getStatus())) throw conflito("Execução não está em andamento");
        ItemTreino item = t.getItens().stream().filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst().orElseThrow(this::naoEncontrado);
        SerieExecutada serie = series.findById(serieId).orElseThrow(this::naoEncontrado);
        if (!Objects.equals(serie.getItemTreino().getId(), itemId)) throw naoEncontrado();
        if (!Objects.equals(serie.getNumeroSerie(), entrada.numeroSerie()))
            throw conflito("A numeração da série não pode ser alterada");
        serie.setCargaUtilizada(entrada.carga()); serie.setRepeticoesRealizadas(entrada.repeticoes());
        serie.setHorarioConclusao(OffsetDateTime.now());
        series.flush();
        return serieResposta(serie);
    }

    @Transactional
    public ApiDtos.TreinoResposta finalizar(String email, Long id) {
        Treino t = treinoBloqueado(id, dono(email).getId());
        if (t.isModelo() || !"EM_ANDAMENTO".equals(t.getStatus())) throw conflito("Execução já finalizada ou inválida");
        Map<Long, List<SerieExecutada>> porItem = seriesPorItem(List.of(id));
        if (porItem.isEmpty()) throw conflito("Registre ao menos uma série antes de finalizar");
        Map<Long, BigDecimal> volumeExercicio = new HashMap<>();
        Set<Long> exerciciosRealizados = new HashSet<>();
        for (ItemTreino item : t.getItens()) {
            List<SerieExecutada> feitas = porItem.getOrDefault(item.getId(), List.of());
            if (feitas.isEmpty()) continue;
            item.setConcluido(true);
            Exercicio e = item.getExercicio();
            exerciciosRealizados.add(e.getId());
            for (SerieExecutada s : feitas) {
                BigDecimal volume = s.getCargaUtilizada().multiply(BigDecimal.valueOf(s.getRepeticoesRealizadas()));
                volumeExercicio.merge(e.getId(), volume, BigDecimal::add);
                if (e.getPraticante() != null && (e.getRecordeCarga() == null || s.getCargaUtilizada().compareTo(e.getRecordeCarga()) > 0)) {
                    e.setRecordeCarga(s.getCargaUtilizada());
                    e.setRecordeRepsParaCarga(s.getRepeticoesRealizadas());
                } else if (e.getPraticante() != null && s.getCargaUtilizada().compareTo(e.getRecordeCarga()) == 0
                        && (e.getRecordeRepsParaCarga() == null || s.getRepeticoesRealizadas() > e.getRecordeRepsParaCarga())) {
                    e.setRecordeRepsParaCarga(s.getRepeticoesRealizadas());
                }
            }
        }
        for (ItemTreino item : t.getItens()) {
            Exercicio e = item.getExercicio();
            BigDecimal volume = volumeExercicio.get(e.getId());
            if (e.getPraticante() != null && volume != null && (e.getRecordeVolumeSessao() == null || volume.compareTo(e.getRecordeVolumeSessao()) > 0))
                e.setRecordeVolumeSessao(volume);
            if (e.getPraticante() != null && exerciciosRealizados.remove(e.getId()))
                e.setTotalVezesRealizado(e.getTotalVezesRealizado() + 1);
        }
        t.setStatus("CONCLUIDO"); t.setFinalizadoEm(OffsetDateTime.now());
        treinos.flush();
        return resposta(t, porItem);
    }

    private Map<Long, List<SerieExecutada>> seriesPorItem(List<Long> ids) {
        Map<Long, List<SerieExecutada>> mapa = new HashMap<>();
        if (ids.isEmpty()) return mapa;
        for (SerieExecutada s : series.findForTreinos(ids))
            mapa.computeIfAbsent(s.getItemTreino().getId(), k -> new ArrayList<>()).add(s);
        return mapa;
    }
    private ApiDtos.SerieResposta serieResposta(SerieExecutada s) {
        // A coluna é gerada pelo banco; para respostas imediatas usa-se a mesma fórmula.
        BigDecimal volume = s.getCargaUtilizada().multiply(BigDecimal.valueOf(s.getRepeticoesRealizadas()));
        return new ApiDtos.SerieResposta(s.getId(), s.getNumeroSerie(), s.getCargaUtilizada(), s.getRepeticoesRealizadas(), volume);
    }
    private ApiDtos.TreinoResposta resposta(Treino t, Map<Long, List<SerieExecutada>> porItem) {
        List<ApiDtos.ItemResposta> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        List<ItemTreino> ordenados = new ArrayList<>(t.getItens());
        ordenados.sort(Comparator.comparing(ItemTreino::getOrdem));
        for (ItemTreino i : ordenados) {
            List<ApiDtos.SerieResposta> feitas = porItem.getOrDefault(i.getId(), List.of()).stream().map(this::serieResposta).toList();
            BigDecimal volumeItem = BigDecimal.ZERO;
            for (ApiDtos.SerieResposta s : feitas) volumeItem = volumeItem.add(s.volume());
            total = total.add(volumeItem);
            itens.add(new ApiDtos.ItemResposta(i.getId(), i.getExercicio().getId(), i.getExercicio().getNome(),
                    i.getOrdem(), i.getSeriesPrevistas(), i.getRepsPrevistas(), i.getCargaPrevista(), i.isConcluido(), feitas, volumeItem));
        }
        return new ApiDtos.TreinoResposta(t.getId(), t.getTreinoOrigem() == null ? null : t.getTreinoOrigem().getId(),
                t.getIdentificacao(), t.isModelo(), t.getStatus(), t.getIniciadoEm(), t.getFinalizadoEm(), itens, total);
    }

    // A lista é reconstruída por requisição para não manter entidades JPA em cache.
    private ListaDuplaTreinos<Sessao> historico(List<Treino> concluidos, Map<Long, List<SerieExecutada>> porItem) {
        List<Sessao> sessoes = new ArrayList<>();
        for (Treino t : concluidos) {
            List<Serie> dados = new ArrayList<>();
            for (ItemTreino i : t.getItens()) for (SerieExecutada s : porItem.getOrDefault(i.getId(), List.of())) {
                GrupoMuscularEnum grupo;
                try { grupo = GrupoMuscularEnum.valueOf(i.getExercicio().getGrupoMuscular()); }
                catch (IllegalArgumentException erro) { grupo = null; }
                dados.add(new Serie(i.getExercicio().getId(), grupo, s.getCargaUtilizada().doubleValue(),
                        s.getRepeticoesRealizadas(), s.getCargaUtilizada()));
            }
            sessoes.add(new Sessao(t.getId(), t.getFinalizadoEm().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime(), dados));
        }
        return new CarregadorHistorico<Sessao>().carregar(sessoes);
    }
    private record Serie(Long getExercicioId, GrupoMuscularEnum getGrupoMuscular,
                         double getCarga, int getRepeticoes, BigDecimal cargaExata) implements SerieHistorico {
        public BigDecimal getCargaDecimal() { return cargaExata; }
    }
    private record Sessao(Long getId, LocalDateTime getDataRealizacao,
                          List<Serie> getSeriesExecutadas) implements TreinoComSeries {
        public boolean isModelo() { return false; }
        public boolean possuiExercicio(Long id) {
            for (Serie s : getSeriesExecutadas) if (Objects.equals(s.getExercicioId(), id)) return true;
            return false;
        }
    }

    private List<Treino> concluidos(Long donoId) {
        return treinos.findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(donoId).stream()
                .filter(t -> "CONCLUIDO".equals(t.getStatus()) && t.getFinalizadoEm() != null).toList();
    }
    @Transactional(readOnly = true)
    public List<ApiDtos.TreinoResposta> historico(String email) {
        List<Treino> ts = concluidos(dono(email).getId());
        Map<Long, List<SerieExecutada>> mapa = seriesPorItem(ts.stream().map(Treino::getId).toList());
        ListaDuplaTreinos<Sessao> lista = historico(ts, mapa);
        Map<Long, Treino> porId = new HashMap<>();
        for (Treino t : ts) porId.put(t.getId(), t);
        List<ApiDtos.TreinoResposta> saida = new ArrayList<>();
        NoTreino<Sessao> atual = lista.getInicio();
        while (atual != null) {
            saida.add(resposta(porId.get(atual.getTreino().getId()), mapa));
            atual = atual.getProximo();
        }
        return saida;
    }
    @Transactional(readOnly = true)
    public ApiDtos.TreinoResposta anterior(String email, Long sessaoId, Long exercicioId) {
        Long donoId = dono(email).getId();
        exercicio(exercicioId, donoId);
        List<Treino> ts = concluidos(donoId);
        Map<Long, List<SerieExecutada>> mapa = seriesPorItem(ts.stream().map(Treino::getId).toList());
        ListaDuplaTreinos<Sessao> lista = historico(ts, mapa);
        NoTreino<Sessao> atual = lista.getInicio();
        while (atual != null && !Objects.equals(atual.getTreino().getId(), sessaoId)) atual = atual.getProximo();
        if (atual == null) throw naoEncontrado();
        Sessao anterior = lista.obterTreinoAnteriorPorExercicio(atual.getTreino(), exercicioId);
        if (anterior == null) throw naoEncontrado();
        for (Treino t : ts) if (Objects.equals(t.getId(), anterior.getId())) return resposta(t, mapa);
        throw naoEncontrado();
    }
    @Transactional(readOnly = true)
    public ApiDtos.ComparacaoResposta comparar(String email, Long id1, Long id2, Long exercicioId) {
        List<Treino> ts = concluidos(dono(email).getId());
        Map<Long, List<SerieExecutada>> mapa = seriesPorItem(ts.stream().map(Treino::getId).toList());
        ListaDuplaTreinos<Sessao> lista = historico(ts, mapa);
        ComparacaoVolumeDecimal c = lista.compararVolumeDecimal(id1, id2, exercicioId);
        if (c == null) throw naoEncontrado();
        String desempenho = "SEM_DADOS";
        ArrayList<DadosSerie> a = new ArrayList<>(), b = new ArrayList<>();
        NoTreino<Sessao> atual = lista.getInicio();
        while (atual != null) {
            if (Objects.equals(id1, atual.getTreino().getId()) || Objects.equals(id2, atual.getTreino().getId()))
                for (Serie s : atual.getTreino().getSeriesExecutadas()) if (Objects.equals(exercicioId, s.getExercicioId()))
                    (Objects.equals(id1, atual.getTreino().getId()) ? a : b).add(new DadosSerie(s.getCarga(), s.getRepeticoes()));
            atual = atual.getProximo();
        }
        desempenho = new ComparativoDesempenho().compararExercicios(a, b);
        return new ApiDtos.ComparacaoResposta(id1, id2, exercicioId, c.volumeSessao1(),
                c.volumeSessao2(), c.diferenca(), desempenho);
    }
    @Transactional(readOnly = true)
    public Integer maiorReps(String email, Long exercicioId, BigDecimal carga) {
        Long donoId = dono(email).getId();
        exercicio(exercicioId, donoId);
        List<Treino> ts = concluidos(donoId);
        return historico(ts, seriesPorItem(ts.stream().map(Treino::getId).toList()))
                .obterMaiorRepsParaCarga(exercicioId, carga);
    }
    @Transactional(readOnly = true)
    public ApiDtos.RecordeResposta recordes(String email, Long exercicioId) {
        Long donoId = dono(email).getId();
        exercicio(exercicioId, donoId);
        List<Treino> ts = concluidos(donoId);
        Map<Long, List<SerieExecutada>> mapa = seriesPorItem(ts.stream().map(Treino::getId).toList());
        BigDecimal maiorCarga = null, maiorVolume = null;
        Integer reps = null;
        int sessoes = 0;
        for (Treino t : ts) {
            BigDecimal volume = BigDecimal.ZERO;
            boolean realizado = false;
            for (ItemTreino i : t.getItens()) if (Objects.equals(i.getExercicio().getId(), exercicioId))
                for (SerieExecutada s : mapa.getOrDefault(i.getId(), List.of())) {
                    realizado = true;
                    volume = volume.add(s.getCargaUtilizada().multiply(BigDecimal.valueOf(s.getRepeticoesRealizadas())));
                    if (maiorCarga == null || s.getCargaUtilizada().compareTo(maiorCarga) > 0) {
                        maiorCarga = s.getCargaUtilizada(); reps = s.getRepeticoesRealizadas();
                    } else if (s.getCargaUtilizada().compareTo(maiorCarga) == 0 && s.getRepeticoesRealizadas() > reps)
                        reps = s.getRepeticoesRealizadas();
                }
            if (realizado) {
                sessoes++;
                if (maiorVolume == null || volume.compareTo(maiorVolume) > 0) maiorVolume = volume;
            }
        }
        return new ApiDtos.RecordeResposta(exercicioId, maiorCarga, reps, maiorVolume, sessoes);
    }
    @Transactional(readOnly = true)
    public ApiDtos.VolumeExercicioResposta volumeExercicio(String email, Long exercicioId) {
        Long donoId = dono(email).getId();
        exercicio(exercicioId, donoId);
        List<Treino> ts = concluidos(donoId);
        Map<Long, List<SerieExecutada>> mapa = seriesPorItem(ts.stream().map(Treino::getId).toList());
        BigDecimal total = BigDecimal.ZERO;
        for (Treino t : ts) for (ItemTreino i : t.getItens()) if (Objects.equals(i.getExercicio().getId(), exercicioId))
            for (SerieExecutada s : mapa.getOrDefault(i.getId(), List.of()))
                total = total.add(s.getCargaUtilizada().multiply(BigDecimal.valueOf(s.getRepeticoesRealizadas())));
        return new ApiDtos.VolumeExercicioResposta(exercicioId, total);
    }
    @Transactional(readOnly = true)
    public ApiDtos.DashboardResposta dashboard(String email) {
        List<Treino> ts = concluidos(dono(email).getId());
        Map<Long, List<SerieExecutada>> mapa = seriesPorItem(ts.stream().map(Treino::getId).toList());
        ListaDuplaTreinos<Sessao> lista = historico(ts, mapa);
        Map<String, BigDecimal> grupos = new LinkedHashMap<>();
        for (var entry : lista.obterVolumePorGrupoMuscularDecimal().entrySet())
            grupos.put(entry.getKey().name(), entry.getValue());
        BigDecimal total = BigDecimal.ZERO;
        for (Treino t : ts) for (ItemTreino i : t.getItens()) for (SerieExecutada s : mapa.getOrDefault(i.getId(), List.of())) {
            BigDecimal v = s.getCargaUtilizada().multiply(BigDecimal.valueOf(s.getRepeticoesRealizadas()));
            total = total.add(v);
        }
        return new ApiDtos.DashboardResposta(lista.getTamanho(), total, grupos);
    }
}
