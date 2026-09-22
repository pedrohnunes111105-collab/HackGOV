package br.com.hackgov.exception;

/** Lançada quando há tentativa de trocar/cancelar agendamento sem confirmação do paciente (T11). */
public class OperacaoNaoAutorizadaException extends RuntimeException {
    public OperacaoNaoAutorizadaException(String mensagem) {
        super(mensagem);
    }
}
