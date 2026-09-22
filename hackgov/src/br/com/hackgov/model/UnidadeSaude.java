package br.com.hackgov.model;

public class UnidadeSaude {
    private final int id;
    private final String nome;
    private final String endereco;

    public UnidadeSaude(int id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }

    @Override
    public String toString() {
        return nome + " - " + endereco;
    }
}
