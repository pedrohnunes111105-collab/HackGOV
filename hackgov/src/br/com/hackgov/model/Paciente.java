package br.com.hackgov.model;

public class Paciente {
    private final int id;
    private final String nome;
    private final String cpf;
    private final String email;

    public Paciente(int id, String nome, String cpf, String email) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do paciente é obrigatório.");
        if (cpf == null || cpf.replaceAll("\\D", "").length() != 11) throw new IllegalArgumentException("CPF inválido.");
        this.id = id;
        this.nome = nome;
        this.cpf = cpf.replaceAll("\\D", "");
        this.email = email;
    }

    /** Dado sensível: nunca exibir o CPF completo em telas ou logs. */
    public String getCpfMascarado() {
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return nome + " (CPF " + getCpfMascarado() + ")";
    }
}
