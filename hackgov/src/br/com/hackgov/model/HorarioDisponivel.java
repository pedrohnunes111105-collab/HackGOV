package br.com.hackgov.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Horário de agenda de um especialista em uma unidade (T09). */
public class HorarioDisponivel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int id;
    private final Especialista especialista;
    private final UnidadeSaude unidade;
    private final LocalDateTime dataHora;
    private boolean disponivel = true;

    public HorarioDisponivel(int id, Especialista especialista, UnidadeSaude unidade, LocalDateTime dataHora) {
        this.id = id;
        this.especialista = especialista;
        this.unidade = unidade;
        this.dataHora = dataHora;
    }

    public void reservar() {
        if (!disponivel) throw new IllegalStateException("Horário " + id + " já está ocupado.");
        disponivel = false;
    }

    public void liberar() {
        disponivel = true;
    }

    public int getId() { return id; }
    public Especialista getEspecialista() { return especialista; }
    public UnidadeSaude getUnidade() { return unidade; }
    public LocalDateTime getDataHora() { return dataHora; }
    public boolean isDisponivel() { return disponivel; }

    @Override
    public String toString() {
        return "[" + id + "] " + dataHora.format(FMT) + " | " + especialista.getNome() + " | " + unidade.getNome();
    }
}
