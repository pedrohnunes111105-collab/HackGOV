package br.com.hackgov.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro imutável de cada mudança de status.
 * Garante rastreabilidade, histórico e auditoria (contexto público).
 */
public class RegistroStatus {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final StatusPedido statusAnterior;
    private final StatusPedido statusNovo;
    private final LocalDateTime dataHora;
    private final String responsavel;
    private final String motivo;

    public RegistroStatus(StatusPedido statusAnterior, StatusPedido statusNovo, String responsavel, String motivo) {
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataHora = LocalDateTime.now();
        this.responsavel = responsavel;
        this.motivo = motivo;
    }

    public StatusPedido getStatusAnterior() { return statusAnterior; }
    public StatusPedido getStatusNovo() { return statusNovo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getResponsavel() { return responsavel; }
    public String getMotivo() { return motivo; }

    @Override
    public String toString() {
        String de = statusAnterior == null ? "-" : statusAnterior.getDescricao();
        return dataHora.format(FMT) + " | " + de + " -> " + statusNovo.getDescricao()
                + " | por: " + responsavel + " | motivo: " + motivo;
    }
}
