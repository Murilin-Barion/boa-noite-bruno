package org.example.view;

import org.example.controller.LoginController;
import org.example.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

public class LoginScreen extends JFrame {
    private JTextField userField;
    private JPasswordField passwordField;
    private LoginController loginController;

    public LoginScreen() {
        this.loginController = new LoginController();

        setTitle("Login");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Gerenciador Financeiro");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        userField = new JTextField(20);
        formPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("ENTRAR");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(50, 150, 250));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> {
            String email = userField.getText();
            String senha = new String(passwordField.getPassword());

            try {
                Usuario usuario = loginController.login(email, senha);

                if (usuario == null) {
                    JOptionPane.showMessageDialog(LoginScreen.this, "Email ou senha incorretos.", "Erro de Login", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        new FinanceManagerScreen(usuario).setVisible(true);
                        dispose();
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(LoginScreen.this, "Erro ao inicializar a tela financeira: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(LoginScreen.this, "Erro ao tentar fazer login: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JButton registerButton = new JButton("CADASTRAR");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(50, 150, 250));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> {
            new RegisterScreen().setVisible(true);
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
}

