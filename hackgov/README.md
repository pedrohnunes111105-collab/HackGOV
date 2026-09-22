# HackGov - Agendamento de Pedidos Médicos (Sprint 1)

## Como rodar no VS Code
1. Instale o JDK 17+ e a extensão "Extension Pack for Java".
2. Abra a pasta `hackgov` no VS Code (File > Open Folder).
3. Abra `src/br/com/hackgov/Main.java` e clique em "Run" acima do `main`.

Pelo terminal:
    javac -encoding UTF-8 -d out $(find src -name "*.java")
    java -cp out br.com.hackgov.Main

## Estrutura
- model/     Paciente, Especialista, UnidadeSaude, HorarioDisponivel, PedidoMedico, Agendamento, StatusPedido, RegistroStatus
- service/   AgendaService (horários simulados), AgendamentoService (fluxo e validações)
- exception/ AcessoNegadoException, OperacaoNaoAutorizadaException
- Main.java  Cenários demonstráveis

## Rastreabilidade (tarefa -> código)
- T03/T04  model/* e AgendamentoService.confirmarAgendamento
- T07      AgendamentoService.consultarStatus + StatusPedido.getOrientacao
- T09      AgendaService.listarDisponiveis
- T11      AgendamentoService.trocarHorario / cancelarAgendamento (confirmação obrigatória)
- T13      AgendamentoService.buscarPedidoComAcesso + PedidoMedico.pertenceA
- T14      Main.java (cenários 1 a 7)
