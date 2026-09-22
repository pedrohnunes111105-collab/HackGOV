package br.com.hackgov.exception;

/** Lançada quando um usuário tenta acessar dados de outro paciente (T13). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
