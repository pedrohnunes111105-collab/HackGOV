package br.com.hackgov.service;

import br.com.hackgov.exception.AcessoNegadoException;
import br.com.hackgov.exception.OperacaoNaoAutorizadaException;
import br.com.hackgov.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fluxo principal: criar pedido, confirmar agendamento (S-01),
 * acompanhar status (S-02) e aplicar as regras de segurança (S-04, S-05).
 */
public class AgendamentoService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AgendaService agendaService;
    private final List<PedidoMedico> pedidos = new ArrayList<>();
    private final List<Agendamento> agendamentos = new ArrayList<>();
    private final List<String> logAuditoria = new ArrayList<>();
    private int proximoPedidoId = 1;
    private int proximoAgendamentoId = 1;

    public AgendamentoService(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    public PedidoMedico criarPedido(Paciente paciente, String especialidade, String descricao) {
        PedidoMedico pedido = new PedidoMedico(proximoPedidoId++, paciente, especialidade, descricao);
        pedidos.add(pedido);
        pedido.alterarStatus(StatusPedido.AGUARDANDO_AGENDA, "Sistema", "Encaminhado para agendamento");
        auditar("Pedido #" + pedido.getId() + " criado para paciente id " + paciente.getId());
        return pedido;
    }

    /** T04 - Confirmar agendamento. */
    public Agendamento confirmarAgendamento(Paciente solicitante, int pedidoId, int horarioId) {
        PedidoMedico pedido = buscarPedidoComAcesso(solicitante, pedidoId);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_AGENDA) {
            throw new IllegalStateException("Pedido #" + pedidoId + " não está aguardando agenda (status: "
                    + pedido.getStatus().getDescricao() + ").");
        }

        HorarioDisponivel horario = agendaService.buscarPorId(horarioId);
        if (!horario.getEspecialista().atende(pedido.getEspecialidade())) {
            throw new IllegalArgumentException("Horário escolhido não é da especialidade do pedido.");
        }

        horario.reservar();
        Agendamento agendamento = new Agendamento(proximoAgendamentoId++, pedido, horario);
        agendamentos.add(agendamento);
        pedido.alterarStatus(StatusPedido.AGENDADO, "Paciente", "Agendamento confirmado no horário " + horarioId);
        auditar("Agendamento #" + agendamento.getId() + " confirmado pelo paciente id " + solicitante.getId());
        return agendamento;
    }

    /** T07 - Consulta simples de status com orientação ao paciente. */
    public String consultarStatus(Paciente solicitante, int pedidoId) {
        PedidoMedico pedido = buscarPedidoComAcesso(solicitante, pedidoId);
        auditar("Status do pedido #" + pedidoId + " consultado pelo paciente id " + solicitante.getId());
        return "Pedido #" + pedido.getId() + " (" + pedido.getEspecialidade() + ")\n"
                + "Status: " + pedido.getStatus().getDescricao() + "\n"
                + "Orientação: " + pedido.getStatus().getOrientacao();
    }

    /** T11 - Troca de horário só com confirmação explícita do paciente. */
    public void trocarHorario(Paciente solicitante, int pedidoId, int novoHorarioId,
                              boolean confirmacaoPaciente, String origem) {
        PedidoMedico pedido = buscarPedidoComAcesso(solicitante, pedidoId);
        Agendamento agendamento = buscarAgendamentoAtivo(pedido);

        if (!confirmacaoPaciente) {
            auditar("BLOQUEADO: troca do pedido #" + pedidoId + " solicitada por " + origem + " sem confirmação do paciente");
            throw new OperacaoNaoAutorizadaException(
                    "Troca bloqueada: é necessária confirmação explícita do paciente. Agendamento original mantido.");
        }

        HorarioDisponivel novo = agendaService.buscarPorId(novoHorarioId);
        if (!novo.getEspecialista().atende(pedido.getEspecialidade())) {
            throw new IllegalArgumentException("Horário escolhido não é da especialidade do pedido.");
        }
        agendamento.trocarHorario(novo);
        pedido.alterarStatus(StatusPedido.AGENDADO, origem + " (confirmado pelo paciente)",
                "Troca para o horário " + novoHorarioId);
        auditar("Troca do pedido #" + pedidoId + " para horário " + novoHorarioId + " confirmada pelo paciente");
    }

    /** T11 - Cancelamento só com confirmação explícita do paciente. */
    public void cancelarAgendamento(Paciente solicitante, int pedidoId, boolean confirmacaoPaciente, String origem) {
        PedidoMedico pedido = buscarPedidoComAcesso(solicitante, pedidoId);
        Agendamento agendamento = buscarAgendamentoAtivo(pedido);

        if (!confirmacaoPaciente) {
            auditar("BLOQUEADO: cancelamento do pedido #" + pedidoId + " solicitado por " + origem + " sem confirmação do paciente");
            throw new OperacaoNaoAutorizadaException(
                    "Cancelamento bloqueado: é necessária confirmação explícita do paciente. Agendamento original mantido.");
        }

        agendamento.cancelar();
        pedido.alterarStatus(StatusPedido.CANCELADO, origem + " (confirmado pelo paciente)", "Cancelado a pedido do paciente");
        auditar("Cancelamento do pedido #" + pedidoId + " confirmado pelo paciente");
    }

    /** T13 - Isolamento: paciente só acessa os próprios pedidos. */
    private PedidoMedico buscarPedidoComAcesso(Paciente solicitante, int pedidoId) {
        PedidoMedico pedido = pedidos.stream()
                .filter(p -> p.getId() == pedidoId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pedido #" + pedidoId + " não encontrado."));

        if (!pedido.pertenceA(solicitante)) {
            auditar("BLOQUEADO: paciente id " + solicitante.getId() + " tentou acessar pedido #" + pedidoId + " de outro paciente");
            throw new AcessoNegadoException("Acesso negado: este pedido pertence a outro paciente.");
        }
        return pedido;
    }

    private Agendamento buscarAgendamentoAtivo(PedidoMedico pedido) {
        return agendamentos.stream()
                .filter(a -> a.getPedido().getId() == pedido.getId() && a.isAtivo())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Pedido #" + pedido.getId() + " não possui agendamento ativo."));
    }

    public Agendamento buscarAgendamento(Paciente solicitante, int pedidoId) {
        return buscarAgendamentoAtivo(buscarPedidoComAcesso(solicitante, pedidoId));
    }

    public List<RegistroStatus> historicoDoPedido(Paciente solicitante, int pedidoId) {
        return buscarPedidoComAcesso(solicitante, pedidoId).getHistorico();
    }

    private void auditar(String evento) {
        logAuditoria.add(LocalDateTime.now().format(FMT) + " | " + evento);
    }

    public List<String> getLogAuditoria() {
        return Collections.unmodifiableList(logAuditoria);
    }
}
