package com.fittrack.fittrack_frontend.service;

import java.math.BigDecimal;
import java.util.Map;

public final class DashboardModels {
    private DashboardModels() { }
    public record DashboardResposta(int sessoesConcluidas, BigDecimal volumeTotal, Map<String, BigDecimal> volumePorGrupo) { }
}
