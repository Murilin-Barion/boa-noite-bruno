package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        Usuario murilo_dados = new Usuario("murilo", "murilo.barion", "123");
        Usuario admin = new Usuario("admin", "admin", "admin");
        BancoDados.adicionarUsuario(murilo_dados);
        BancoDados.adicionarUsuario(admin);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen();
            }
        });
    }
}