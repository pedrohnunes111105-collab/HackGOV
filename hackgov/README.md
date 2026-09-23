# HackGov - Agendamento de Pedidos Médicos

## Como rodar no VS Code
1. Instale o JDK 17+ e a extensão "Extension Pack for Java".
2. Abra a pasta `hackgov` no VS Code (File > Open Folder).
3. Abra `src/br/com/hackgov/Main.java` e clique em "Run" acima do `main`.
   Para os cenários automáticos da Sprint 1, rode `Demonstracao.java`.

Pelo terminal:
    javac -encoding UTF-8 -d out $(find src -name "*.java")
    java -cp out br.com.hackgov.Main            # menu interativo
    java -cp out br.com.hackgov.Demonstracao    # cenários automáticos

## Menu interativo
Pacientes de exemplo (dados fictícios): Maria Silva - CPF 12345678900, José Santos - CPF 98765432100.
Também é possível cadastrar um novo paciente. Depois de entrar com o CPF, o paciente pode:
criar pedido, ver seus pedidos com status e orientação, ver horários, agendar, trocar horário,
cancelar e ver o histórico do pedido. Troca e cancelamento pedem confirmação (s/n).
No menu inicial, a opção "Log de auditoria (gestor)" mostra todos os eventos registrados.

Os dados ficam em memória: ao fechar o programa, tudo é apagado.

## Estrutura
- model/     Paciente, Especialista, UnidadeSaude, HorarioDisponivel, PedidoMedico, Agendamento, StatusPedido, RegistroStatus
- service/   AgendaService (horários simulados), AgendamentoService (fluxo e validações)
- exception/ AcessoNegadoException, OperacaoNaoAutorizadaException
- ui/        MenuTerminal (menu interativo)
- Main.java  Abre o menu interativo
- Demonstracao.java  Cenários demonstráveis da Sprint 1

## Rastreabilidade (tarefa -> código)
- T03/T04  model/* e AgendamentoService.confirmarAgendamento
- T07      AgendamentoService.consultarStatus + StatusPedido.getOrientacao
- T09      AgendaService.listarDisponiveis
- T11      AgendamentoService.trocarHorario / cancelarAgendamento (confirmação obrigatória)
- T13      AgendamentoService.buscarPedidoComAcesso / listarPedidos + PedidoMedico.pertenceA
- T14      Demonstracao.java (cenários 1 a 7)
