package com.hospital.entidades;

/**
 * Regras de validação e formatação de CPF.
 *
 * <p>O CPF é aceito com ou sem máscara ("529.982.247-25" ou "52998224725") e sempre
 * armazenado no formato com máscara, para que a verificação de CPF único não dependa
 * da forma como o usuário digitou o número.
 */
public final class Cpf {

    private Cpf() {
    }

    /** Remove tudo o que não for dígito. */
    public static String somenteDigitos(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    /**
     * Verifica se o CPF possui 11 dígitos, não é uma sequência repetida (ex.: 111.111.111-11)
     * e se os dois dígitos verificadores conferem.
     */
    public static boolean isValido(String cpf) {
        String digitos = somenteDigitos(cpf);
        if (cpf == null || !cpf.trim().matches("[0-9.\\-\\s]+") || digitos.length() != 11) {
            return false;
        }
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }
        return calcularDigito(digitos, 9) == digitos.charAt(9) - '0'
                && calcularDigito(digitos, 10) == digitos.charAt(10) - '0';
    }

    /** Devolve o CPF no formato 000.000.000-00. Assume um CPF já validado. */
    public static String formatar(String cpf) {
        String d = somenteDigitos(cpf);
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9, 11);
    }

    private static int calcularDigito(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
