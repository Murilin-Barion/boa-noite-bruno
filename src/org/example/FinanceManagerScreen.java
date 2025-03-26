package org.example;

import javax.swing.*;
import java.awt.*;

public class FinanceManagerScreen extends JFrame {
    private JTextField valueField, dateField, descriptionField;
    private JComboBox<String> categoryComboBox, typeComboBox;
    private JLabel balanceLabel, incomeLabel, expenseLabel;

    public FinanceManagerScreen(Usuario user) {
        setTitle("Gerenciador Financeiro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Barra de menu do usuário
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("Usuário: " + user.getNome());
        JMenuItem logoutItem = new JMenuItem("Sair");
        logoutItem.addActionListener(e -> {
            this.dispose();
            new LoginScreen().setVisible(true);
        });
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel para o formulário de transações
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastro de Transações"));

        formPanel.add(new JLabel("Tipo:"));
        typeComboBox = new JComboBox<>(new String[]{"Receita (+)", "Despesa (-)"});
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Categoria:"));
        categoryComboBox = new JComboBox<>();
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Data:"));
        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Descrição:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Valor:"));
        valueField = new JTextField();
        formPanel.add(valueField);

        JButton addTransactionButton = new JButton("Adicionar Transação");
        addTransactionButton.setPreferredSize(new Dimension(10, 30));
        formPanel.add(addTransactionButton);

        // Painel para o histórico de transações
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        JList<String> transactionList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(transactionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // Painel para o resumo financeiro
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(3, 1, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        balanceLabel = new JLabel("Saldo Total: R$ 0.00");
        incomeLabel = new JLabel("Receitas: R$ 0.00");
        expenseLabel = new JLabel("Despesas: R$ 0.00");

        summaryPanel.add(balanceLabel);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        // Painel para gerenciamento de categorias
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Categorias"));

        JTextField newCategoryField = new JTextField();
        newCategoryField.setPreferredSize(new Dimension(100, 150));
        categoryPanel.add(newCategoryField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        Dimension buttonSize = new Dimension(150, 30);
        addCategoryButton.setPreferredSize(buttonSize);
        editCategoryButton.setPreferredSize(buttonSize);
        deleteCategoryButton.setPreferredSize(buttonSize);

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(editCategoryButton);
        buttonPanel.add(deleteCategoryButton);

        categoryPanel.add(buttonPanel, BorderLayout.CENTER);

        // Adicionando os painéis ao painel principal
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(10, 10));
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(categoryPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(10, 10));
        rightPanel.add(historyPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

}