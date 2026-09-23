package br.com.hackgov;

import br.com.hackgov.model.*;
import br.com.hackgov.service.AgendaService;
import br.com.hackgov.service.AgendamentoService;

/** T14 - Cenário demonstrável: consultar agenda, confirmar agendamento e acompanhar status. */
public class Demonstracao {

    public static void main(String[] args) {
        AgendaService agendaService = new AgendaService();
        AgendamentoService service = new AgendamentoService(agendaService);

        Paciente maria = new Paciente(1, "Maria Silva", "123.456.789-00", "maria@email.com");
        Paciente jose = new Paciente(2, "José Santos", "987.654.321-00", "jose@email.com");

        titulo("CENÁRIO 1 - Paciente cria pedido médico");
        PedidoMedico pedido = service.criarPedido(maria, "Cardiologia", "Encaminhamento do clínico geral");
        System.out.println("Paciente: " + maria);
        System.out.println(service.consultarStatus(maria, pedido.getId()));

        titulo("CENÁRIO 2 - Consultar agendas disponíveis (Cardiologia)");
        agendaService.listarDisponiveis("Cardiologia").forEach(h -> System.out.println("  " + h));

        titulo("CENÁRIO 3 - Confirmar agendamento no horário 1");
        Agendamento ag = service.confirmarAgendamento(maria, pedido.getId(), 1);
        System.out.println(ag);
        System.out.println(service.consultarStatus(maria, pedido.getId()));

        titulo("CENÁRIO 4 - IA tenta trocar horário SEM confirmação do paciente");
        try {
            service.trocarHorario(maria, pedido.getId(), 2, false, "Assistente IA");
        } catch (RuntimeException e) {
            System.out.println("ERRO ESPERADO: " + e.getMessage());
        }
        System.out.println("Agendamento atual: " + service.buscarAgendamento(maria, pedido.getId()));

        titulo("CENÁRIO 5 - Outro paciente tenta consultar o pedido da Maria");
        try {
            service.consultarStatus(jose, pedido.getId());
        } catch (RuntimeException e) {
            System.out.println("ERRO ESPERADO: " + e.getMessage());
        }

        titulo("CENÁRIO 6 - Troca de horário COM confirmação do paciente");
        service.trocarHorario(maria, pedido.getId(), 2, true, "Assistente IA");
        System.out.println("Agendamento atual: " + service.buscarAgendamento(maria, pedido.getId()));

        titulo("CENÁRIO 7 - Histórico do pedido (rastreabilidade)");
        service.historicoDoPedido(maria, pedido.getId()).forEach(r -> System.out.println("  " + r));

        titulo("LOG DE AUDITORIA");
        service.getLogAuditoria().forEach(l -> System.out.println("  " + l));
    }

    private static void titulo(String texto) {
        System.out.println("\n========== " + texto + " ==========");
    }
}
