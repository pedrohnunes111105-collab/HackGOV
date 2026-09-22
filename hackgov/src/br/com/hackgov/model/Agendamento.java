package br.com.hackgov.model;

import java.time.LocalDateTime;

public class Agendamento {
    private final int id;
    private final PedidoMedico pedido;
    private HorarioDisponivel horario;
    private final LocalDateTime dataConfirmacao;
    private boolean ativo = true;

    public Agendamento(int id, PedidoMedico pedido, HorarioDisponivel horario) {
        this.id = id;
        this.pedido = pedido;
        this.horario = horario;
        this.dataConfirmacao = LocalDateTime.now();
    }

    public void trocarHorario(HorarioDisponivel novoHorario) {
        novoHorario.reservar();
        horario.liberar();
        horario = novoHorario;
    }

    public void cancelar() {
        horario.liberar();
        ativo = false;
    }

    public int getId() { return id; }
    public PedidoMedico getPedido() { return pedido; }
    public HorarioDisponivel getHorario() { return horario; }
    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public boolean isAtivo() { return ativo; }

    @Override
    public String toString() {
        return "Agendamento #" + id + " | Pedido #" + pedido.getId() + " | " + horario
                + " | " + (ativo ? "ATIVO" : "CANCELADO");
    }
}
