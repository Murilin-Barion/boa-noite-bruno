package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        Usuario murilo_dados = new Usuario("murilo", "murilo.barion", "123");
        BancoUsuarios.adicionarUsuario(murilo_dados);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen();
            }
        });
    }
}