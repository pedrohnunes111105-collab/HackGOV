package br.com.hackgov;

import br.com.hackgov.model.Paciente;
import br.com.hackgov.service.AgendaService;
import br.com.hackgov.service.AgendamentoService;
import br.com.hackgov.ui.MenuTerminal;

import java.util.List;

/** Ponto de entrada: abre o menu interativo. Os cenários automáticos estão em Demonstracao. */
public class Main {

    public static void main(String[] args) {
        AgendaService agendaService = new AgendaService();
        AgendamentoService agendamentoService = new AgendamentoService(agendaService);

        List<Paciente> pacientes = List.of(
                new Paciente(1, "Maria Silva", "123.456.789-00", "maria@email.com"),
                new Paciente(2, "José Santos", "987.654.321-00", "jose@email.com"));

        System.out.println("Pacientes de exemplo (dados fictícios): Maria Silva - CPF 12345678900 | José Santos - CPF 98765432100");
        new MenuTerminal(agendaService, agendamentoService, pacientes).iniciar();
    }
}
