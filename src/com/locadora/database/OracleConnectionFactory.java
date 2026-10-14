package com.locadora.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class OracleConnectionFactory {
    private static final String JDBC_URL_VARIABLE = "ORACLE_JDBC_URL";
    private static final String USER_VARIABLE = "ORACLE_USER";
    private static final String PASSWORD_VARIABLE = "ORACLE_PASSWORD";

    private OracleConnectionFactory() {
    }

    public static Connection abrirConexao() throws SQLException {
        String url = lerVariavelObrigatoria(JDBC_URL_VARIABLE);
        if (!url.startsWith("jdbc:oracle:thin:@")) {
            throw new IllegalStateException(
                    JDBC_URL_VARIABLE + " deve usar o formato de conexao jdbc:oracle:thin.");
        }
        return DriverManager.getConnection(
                url,
                lerVariavelObrigatoria(USER_VARIABLE),
                lerVariavelObrigatoria(PASSWORD_VARIABLE));
    }

    private static String lerVariavelObrigatoria(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException("Configure a variavel de ambiente " + nome + ".");
        }
        return valor.trim();
    }
}
