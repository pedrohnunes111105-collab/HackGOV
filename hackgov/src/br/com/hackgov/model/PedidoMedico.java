package br.com.hackgov.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PedidoMedico {
    private final int id;
    private final Paciente paciente;
    private final String especialidade;
    private final String descricao;
    private final LocalDateTime dataCriacao;
    private StatusPedido status;
    private final List<RegistroStatus> historico = new ArrayList<>();

    public PedidoMedico(int id, Paciente paciente, String especialidade, String descricao) {
        if (paciente == null) throw new IllegalArgumentException("Pedido precisa de um paciente.");
        if (especialidade == null || especialidade.isBlank()) throw new IllegalArgumentException("Especialidade é obrigatória.");
        this.id = id;
        this.paciente = paciente;
        this.especialidade = especialidade;
        this.descricao = descricao;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.SOLICITADO;
        historico.add(new RegistroStatus(null, StatusPedido.SOLICITADO, "Paciente", "Pedido criado"));
    }

    /** Toda mudança de status passa por aqui e gera histórico. */
    public void alterarStatus(StatusPedido novoStatus, String responsavel, String motivo) {
        if (status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Pedido cancelado não pode mudar de status.");
        }
        historico.add(new RegistroStatus(status, novoStatus, responsavel, motivo));
        this.status = novoStatus;
    }

    /** Regra de isolamento (T12/T13): o pedido pertence a apenas um paciente. */
    public boolean pertenceA(Paciente p) {
        return p != null && paciente.getId() == p.getId();
    }

    public int getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getEspecialidade() { return especialidade; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusPedido getStatus() { return status; }
    public List<RegistroStatus> getHistorico() { return Collections.unmodifiableList(historico); }
}
