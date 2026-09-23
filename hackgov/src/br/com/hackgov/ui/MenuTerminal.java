package br.com.hackgov.ui;

import br.com.hackgov.exception.OperacaoNaoAutorizadaException;
import br.com.hackgov.model.*;
import br.com.hackgov.service.AgendaService;
import br.com.hackgov.service.AgendamentoService;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Menu interativo no terminal: o paciente usa o sistema escolhendo opções. */
public class MenuTerminal {
    private static final String ORIGEM = "Paciente (terminal)";

    private final AgendaService agendaService;
    private final AgendamentoService agendamentoService;
    private final List<Paciente> pacientes;
    private final Scanner scanner;
    private int proximoPacienteId;

    public MenuTerminal(AgendaService agendaService, AgendamentoService agendamentoService, List<Paciente> pacientes) {
        this.agendaService = agendaService;
        this.agendamentoService = agendamentoService;
        this.pacientes = new ArrayList<>(pacientes);
        this.proximoPacienteId = pacientes.stream().mapToInt(Paciente::getId).max().orElse(0) + 1;
        this.scanner = new Scanner(System.in, charsetDaEntrada());
    }

    public void iniciar() {
        try {
            menuInicial();
        } catch (NoSuchElementException e) {
            // Entrada encerrada (Ctrl+Z / fim do arquivo): sai normalmente.
        }
        System.out.println("\nAté logo!");
    }

    // ---------- Menus ----------

    private void menuInicial() {
        while (true) {
            titulo("HACKGOV - AGENDAMENTO DE CONSULTAS");
            System.out.println("1 - Entrar com CPF");
            System.out.println("2 - Cadastrar paciente");
            System.out.println("3 - Log de auditoria (gestor)");
            System.out.println("0 - Sair");
            switch (lerInteiro("Escolha: ")) {
                case 1 -> {
                    Paciente p = entrar();
                    if (p != null) menuPaciente(p);
                }
                case 2 -> {
                    Paciente p = cadastrar();
                    if (p != null) menuPaciente(p);
                }
                case 3 -> mostrarLogAuditoria();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void menuPaciente(Paciente paciente) {
        while (true) {
            titulo("Olá, " + paciente);
            System.out.println("1 - Novo pedido médico");
            System.out.println("2 - Meus pedidos (status e orientação)");
            System.out.println("3 - Ver horários disponíveis");
            System.out.println("4 - Agendar consulta");
            System.out.println("5 - Trocar horário");
            System.out.println("6 - Cancelar agendamento");
            System.out.println("7 - Histórico de um pedido");
            System.out.println("0 - Sair da conta");
            int opcao = lerInteiro("Escolha: ");
            if (opcao == 0) return;
            try {
                executar(paciente, opcao);
            } catch (NoSuchElementException e) {
                throw e;
            } catch (RuntimeException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    private void executar(Paciente paciente, int opcao) {
        switch (opcao) {
            case 1 -> novoPedido(paciente);
            case 2 -> meusPedidos(paciente);
            case 3 -> verHorarios();
            case 4 -> agendar(paciente);
            case 5 -> trocarHorario(paciente);
            case 6 -> cancelar(paciente);
            case 7 -> historico(paciente);
            default -> System.out.println("Opção inválida.");
        }
    }

    // ---------- Identificação ----------

    private Paciente entrar() {
        String cpf = lerTexto("CPF: ");
        for (Paciente p : pacientes) {
            if (p.possuiCpf(cpf)) return p;
        }
        System.out.println("CPF não cadastrado. Use a opção 2 para se cadastrar.");
        return null;
    }

    private Paciente cadastrar() {
        String nome = lerTexto("Nome completo: ");
        String cpf = lerTexto("CPF: ");
        String email = lerTexto("E-mail: ");
        if (pacientes.stream().anyMatch(p -> p.possuiCpf(cpf))) {
            System.out.println("ERRO: já existe um paciente com este CPF. Use a opção 1 para entrar.");
            return null;
        }
        try {
            Paciente novo = new Paciente(proximoPacienteId, nome, cpf, email);
            proximoPacienteId++;
            pacientes.add(novo);
            System.out.println("Cadastro realizado: " + novo);
            return novo;
        } catch (IllegalArgumentException e) {
            System.out.println("ERRO: " + e.getMessage());
            return null;
        }
    }

    // ---------- Ações do paciente ----------

    private void novoPedido(Paciente paciente) {
        String especialidade = escolherEspecialidade();
        String descricao = lerTexto("Motivo do encaminhamento: ");
        PedidoMedico pedido = agendamentoService.criarPedido(paciente, especialidade, descricao);
        System.out.println("\nPedido criado!");
        System.out.println(agendamentoService.consultarStatus(paciente, pedido.getId()));
    }

    private void meusPedidos(Paciente paciente) {
        List<PedidoMedico> pedidos = agendamentoService.listarPedidos(paciente);
        if (pedidos.isEmpty()) {
            System.out.println("Você ainda não tem pedidos. Use a opção 1 para criar um.");
            return;
        }
        for (PedidoMedico pedido : pedidos) {
            System.out.println();
            System.out.println(agendamentoService.consultarStatus(paciente, pedido.getId()));
            if (pedido.getStatus() == StatusPedido.AGENDADO) {
                System.out.println("Consulta: " + agendamentoService.buscarAgendamento(paciente, pedido.getId()).getHorario());
            }
        }
    }

    private void verHorarios() {
        String especialidade = escolherEspecialidade();
        mostrarHorarios(agendaService.listarDisponiveis(especialidade));
    }

    private void agendar(Paciente paciente) {
        PedidoMedico pedido = escolherPedido(paciente, p -> p.getStatus() == StatusPedido.AGUARDANDO_AGENDA,
                "Nenhum pedido aguardando agenda. Crie um pedido com a opção 1.");
        if (pedido == null) return;

        List<HorarioDisponivel> horarios = agendaService.listarDisponiveis(pedido.getEspecialidade());
        if (!mostrarHorarios(horarios)) return;

        int horarioId = lerInteiro("Número do horário: ");
        Agendamento agendamento = agendamentoService.confirmarAgendamento(paciente, pedido.getId(), horarioId);
        System.out.println("\nConsulta agendada: " + agendamento.getHorario());
        System.out.println("Orientação: " + pedido.getStatus().getOrientacao());
    }

    private void trocarHorario(Paciente paciente) {
        PedidoMedico pedido = escolherPedido(paciente, p -> p.getStatus() == StatusPedido.AGENDADO,
                "Nenhum pedido com consulta agendada.");
        if (pedido == null) return;

        System.out.println("Horário atual: " + agendamentoService.buscarAgendamento(paciente, pedido.getId()).getHorario());
        List<HorarioDisponivel> horarios = agendaService.listarDisponiveis(pedido.getEspecialidade());
        if (!mostrarHorarios(horarios)) return;

        int novoHorarioId = lerInteiro("Número do novo horário: ");
        boolean confirmado = confirmar("Confirma a troca para o horário " + novoHorarioId + "?");
        try {
            agendamentoService.trocarHorario(paciente, pedido.getId(), novoHorarioId, confirmado, ORIGEM);
        } catch (OperacaoNaoAutorizadaException e) {
            System.out.println("Troca não realizada. Seu agendamento foi mantido.");
            return;
        }
        System.out.println("Horário trocado: " + agendamentoService.buscarAgendamento(paciente, pedido.getId()).getHorario());
    }

    private void cancelar(Paciente paciente) {
        PedidoMedico pedido = escolherPedido(paciente, p -> p.getStatus() == StatusPedido.AGENDADO,
                "Nenhum pedido com consulta agendada.");
        if (pedido == null) return;

        System.out.println("Consulta: " + agendamentoService.buscarAgendamento(paciente, pedido.getId()).getHorario());
        boolean confirmado = confirmar("Confirma o cancelamento? Esta ação não pode ser desfeita.");
        try {
            agendamentoService.cancelarAgendamento(paciente, pedido.getId(), confirmado, ORIGEM);
        } catch (OperacaoNaoAutorizadaException e) {
            System.out.println("Cancelamento não realizado. Seu agendamento foi mantido.");
            return;
        }
        System.out.println("Agendamento cancelado. O horário foi liberado para outros pacientes.");
    }

    private void historico(Paciente paciente) {
        PedidoMedico pedido = escolherPedido(paciente, p -> true, "Você ainda não tem pedidos.");
        if (pedido == null) return;
        agendamentoService.historicoDoPedido(paciente, pedido.getId()).forEach(r -> System.out.println("  " + r));
    }

    private void mostrarLogAuditoria() {
        List<String> log = agendamentoService.getLogAuditoria();
        if (log.isEmpty()) {
            System.out.println("Nenhum evento registrado.");
            return;
        }
        log.forEach(l -> System.out.println("  " + l));
    }

    // ---------- Seleções ----------

    private String escolherEspecialidade() {
        List<String> especialidades = agendaService.listarEspecialidades();
        System.out.println("Especialidades:");
        for (int i = 0; i < especialidades.size(); i++) {
            System.out.println("  " + (i + 1) + " - " + especialidades.get(i));
        }
        while (true) {
            int opcao = lerInteiro("Número da especialidade: ");
            if (opcao >= 1 && opcao <= especialidades.size()) return especialidades.get(opcao - 1);
            System.out.println("Opção inválida.");
        }
    }

    /** Lista os pedidos do paciente que atendem ao filtro e pede para escolher um. */
    private PedidoMedico escolherPedido(Paciente paciente, Predicate<PedidoMedico> filtro, String mensagemVazio) {
        List<PedidoMedico> pedidos = agendamentoService.listarPedidos(paciente).stream()
                .filter(filtro)
                .collect(Collectors.toList());
        if (pedidos.isEmpty()) {
            System.out.println(mensagemVazio);
            return null;
        }
        System.out.println("Seus pedidos:");
        pedidos.forEach(p -> System.out.println("  [" + p.getId() + "] " + p.getEspecialidade()
                + " - " + p.getStatus().getDescricao()));
        int id = lerInteiro("Número do pedido: ");
        return pedidos.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pedido #" + id + " não está na lista."));
    }

    /** Mostra os horários; retorna false se não houver nenhum. */
    private boolean mostrarHorarios(List<HorarioDisponivel> horarios) {
        if (horarios.isEmpty()) {
            System.out.println("Nenhum horário disponível para esta especialidade no momento.");
            return false;
        }
        System.out.println("Horários disponíveis:");
        horarios.forEach(h -> System.out.println("  " + h));
        return true;
    }

    // ---------- Entrada ----------

    private String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return scanner.nextLine().trim();
    }

    private int lerInteiro(String rotulo) {
        while (true) {
            String texto = lerTexto(rotulo);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas o número.");
            }
        }
    }

    private boolean confirmar(String pergunta) {
        String resposta = lerTexto(pergunta + " (s/n): ");
        return resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim");
    }

    private static void titulo(String texto) {
        System.out.println("\n========== " + texto + " ==========");
    }

    /** Usa a codificação do console para ler acentos corretamente no Windows. */
    private static Charset charsetDaEntrada() {
        String encoding = System.getProperty("stdin.encoding");
        if (encoding != null) {
            try {
                return Charset.forName(encoding);
            } catch (RuntimeException ignorada) {
                // cai para as opções abaixo
            }
        }
        return System.console() != null ? System.console().charset() : Charset.defaultCharset();
    }
}
