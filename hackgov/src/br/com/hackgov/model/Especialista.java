package br.com.hackgov.model;

public class Especialista {
    private final int id;
    private final String nome;
    private final String especialidade;
    private final String crm;

    public Especialista(int id, String nome, String especialidade, String crm) {
        this.id = id;
        this.nome = nome;
        this.especialidade = especialidade;
        this.crm = crm;
    }

    public boolean atende(String especialidadeBuscada) {
        return especialidade.equalsIgnoreCase(especialidadeBuscada);
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecialidade() { return especialidade; }
    public String getCrm() { return crm; }

    @Override
    public String toString() {
        return nome + " - " + especialidade + " (CRM " + crm + ")";
    }
}
