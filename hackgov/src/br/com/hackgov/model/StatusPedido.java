package br.com.hackgov.model;

/**
 * Estados possíveis de um pedido médico (T05).
 * Cada estado traz uma mensagem de orientação para o paciente (T07).
 */
public enum StatusPedido {
    SOLICITADO("Solicitado", "Seu pedido foi recebido e será analisado."),
    AGUARDANDO_AGENDA("Aguardando agenda", "Seu pedido está aguardando um horário. Consulte as agendas para escolher uma opção."),
    AGENDADO("Agendado", "Seu atendimento está confirmado. Compareça à unidade com documento com foto e cartão do SUS."),
    CANCELADO("Cancelado", "Seu pedido foi cancelado. Em caso de dúvida, procure a unidade de saúde.");

    private final String descricao;
    private final String orientacao;

    StatusPedido(String descricao, String orientacao) {
        this.descricao = descricao;
        this.orientacao = orientacao;
    }

    public String getDescricao() { return descricao; }
    public String getOrientacao() { return orientacao; }
}
