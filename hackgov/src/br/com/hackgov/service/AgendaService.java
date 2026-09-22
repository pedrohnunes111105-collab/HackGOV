package br.com.hackgov.service;

import br.com.hackgov.model.Especialista;
import br.com.hackgov.model.HorarioDisponivel;
import br.com.hackgov.model.UnidadeSaude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Consulta de agendas disponíveis com dados simulados (T08/T09). */
public class AgendaService {
    private final List<HorarioDisponivel> horarios = new ArrayList<>();

    public AgendaService() {
        carregarDadosSimulados();
    }

    private void carregarDadosSimulados() {
        UnidadeSaude ubsCentro = new UnidadeSaude(1, "UBS Centro", "Rua da Saúde, 100");
        UnidadeSaude ubsNorte = new UnidadeSaude(2, "UBS Zona Norte", "Av. Brasil, 2500");

        Especialista cardio = new Especialista(1, "Dra. Ana Lima", "Cardiologia", "12345-SP");
        Especialista orto = new Especialista(2, "Dr. Carlos Souza", "Ortopedia", "67890-SP");
        Especialista cardio2 = new Especialista(3, "Dr. Marcos Reis", "Cardiologia", "54321-SP");

        LocalDateTime base = LocalDateTime.now().plusDays(3).withHour(8).withMinute(0).withSecond(0).withNano(0);

        horarios.add(new HorarioDisponivel(1, cardio, ubsCentro, base));
        horarios.add(new HorarioDisponivel(2, cardio, ubsCentro, base.plusHours(2)));
        horarios.add(new HorarioDisponivel(3, cardio2, ubsNorte, base.plusDays(1).plusHours(1)));
        horarios.add(new HorarioDisponivel(4, orto, ubsNorte, base.plusDays(2)));
        horarios.add(new HorarioDisponivel(5, orto, ubsCentro, base.plusDays(4).plusHours(3)));
    }

    public List<HorarioDisponivel> listarDisponiveis(String especialidade) {
        return horarios.stream()
                .filter(HorarioDisponivel::isDisponivel)
                .filter(h -> h.getEspecialista().atende(especialidade))
                .collect(Collectors.toList());
    }

    public HorarioDisponivel buscarPorId(int id) {
        return horarios.stream()
                .filter(h -> h.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Horário " + id + " não encontrado."));
    }
}
