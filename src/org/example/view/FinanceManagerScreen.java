package org.example.view;

import org.example.controller.FinanceController;
import org.example.model.Categoria;
import org.example.model.Transacao;
import org.example.model.Usuario;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FinanceManagerScreen extends JFrame {
    private JTextField valueField, descriptionField;
    private JFormattedTextField dateField, startDateField, endDateField;
    private JComboBox<String> categoryComboBox, typeComboBox, managerCategoryCombo, filterTypeCombo;
    private JLabel balanceLabel, incomeLabel, expenseLabel;
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;
    private DefaultListModel<String> transactionListModel;
    private JList<String> transactionList;
    private JButton addCategoryButton;
    private JButton addTransactionButton;

    private Usuario currentUser;
    private FinanceController financeController;
    private final DateTimeFormatter viewDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FinanceManagerScreen(Usuario user) throws ParseException {
        this.currentUser = user;
        this.financeController = new FinanceController(user);
        this.currentUser = financeController.getCurrentUser();

        if (this.currentUser == null) {
            JOptionPane.showMessageDialog(this, "Erro fatal ao carregar dados do usuário. Saindo.", "Erro", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
            dispose();
            return;
        }
        if (this.currentUser.getCategorias() == null) {
            this.currentUser.setCategorias(new ArrayList<>());
        }
        if (this.currentUser.getTransacoes() == null) {
            this.currentUser.setTransacoes(new ArrayList<>());
        }

        setTitle("Gerenciador Financeiro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setupUI();

        SwingUtilities.invokeLater(() -> {
            updateFinanceData();
            updateCategoryList();
            updateCategoryComboBoxes();
            updateManagerCategoryComboBox();
        });
    }

    private MaskFormatter createDateFormatter() {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("##/##/####");
            formatter.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            System.err.println("Erro inesperado ao criar MaskFormatter: " + e.getMessage());
        }
        return formatter;
    }

    private void setupUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("Usuário: " + currentUser.getNome());
        JMenuItem logoutItem = new JMenuItem("Sair");
        logoutItem.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        });
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10)); // 6 linhas, 2 colunas
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastro de Transações"));

        formPanel.add(new JLabel("Tipo:"));
        typeComboBox = new JComboBox<>(new String[]{"Receita", "Despesa"});
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Categoria:"));
        categoryComboBox = new JComboBox<>();
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Data:"));
        MaskFormatter originalDateFormatter = createDateFormatter();
        dateField = new JFormattedTextField(originalDateFormatter);
        dateField.setColumns(10);
        formPanel.add(dateField);

        formPanel.add(new JLabel("Descrição:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Valor:"));
        valueField = new JTextField();
        formPanel.add(valueField);

        addTransactionButton = new JButton("Adicionar Transação");
        addTransactionButton.setPreferredSize(new Dimension(10, 30));
        addTransactionButton.addActionListener(e -> addTransactionActionWithWorker());
        formPanel.add(addTransactionButton);
        formPanel.add(new JLabel());

        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrar Transações"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        MaskFormatter filterDateFormatter = createDateFormatter();

        gbc.gridx = 0; gbc.gridy = 0; filterPanel.add(new JLabel("Data Inicial:"), gbc);
        gbc.gridx = 1; startDateField = new JFormattedTextField(filterDateFormatter); startDateField.setColumns(10); filterPanel.add(startDateField, gbc);
        gbc.gridx = 2; filterPanel.add(new JLabel("Data Final:"), gbc);
        gbc.gridx = 3; endDateField = new JFormattedTextField(filterDateFormatter); endDateField.setColumns(10); filterPanel.add(endDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; filterPanel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; filterTypeCombo = new JComboBox<>(new String[]{"Todos", "Receita", "Despesa"}); filterPanel.add(filterTypeCombo, gbc);

        gbc.gridx = 2; filterPanel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 3; managerCategoryCombo = new JComboBox<>();
        filterPanel.add(managerCategoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.CENTER;
        JButton filterButton = new JButton("Aplicar Filtros");
        filterButton.addActionListener(e -> filterTransactionsAction());
        filterPanel.add(filterButton, gbc);

        historyPanel.add(filterPanel, BorderLayout.NORTH);

        transactionListModel = new DefaultListModel<>();
        transactionList = new JList<>(transactionListModel);
        JScrollPane scrollPane = new JScrollPane(transactionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(3, 1, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        balanceLabel = new JLabel("Saldo: R$ 0.00");
        incomeLabel = new JLabel("Receitas: R$ 0.00");
        expenseLabel = new JLabel("Despesas: R$ 0.00");
        summaryPanel.add(balanceLabel);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Categorias"));

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryPanel.add(categoryScrollPane, BorderLayout.CENTER);

        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        addCategoryButton.addActionListener(e -> addCategoryActionWithWorker());
        editCategoryButton.addActionListener(e -> editCategoryAction());
        deleteCategoryButton.addActionListener(e -> deleteCategoryAction());

        categoryButtonPanel.add(addCategoryButton);
        categoryButtonPanel.add(editCategoryButton);
        categoryButtonPanel.add(deleteCategoryButton);

        categoryPanel.add(categoryButtonPanel, BorderLayout.SOUTH);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(categoryPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.add(historyPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);

        mainPanel.setLayout(new GridLayout(1, 2, 10, 10));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    private void addTransactionActionWithWorker() {
        String tipo = (String) typeComboBox.getSelectedItem();
        String categoriaDesc = (String) categoryComboBox.getSelectedItem();
        String dataStrRaw = dateField.getText().replace("/", "").trim();
        String dataStrFormatted = dateField.getText();
        String descricao = descriptionField.getText().trim();
        String valorStr = valueField.getText().trim();

        if (tipo == null || categoriaDesc == null || dataStrRaw.contains("_") || dataStrRaw.length() != 8 || descricao.isEmpty() || valorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos corretamente (Data: dd/MM/yyyy)!", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate data;
        double valor;
        try {
            data = LocalDate.parse(dataStrFormatted, viewDateFormatter);
            valor = Double.parseDouble(valorStr.replace(",", "."));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido. Use dd/MM/yyyy.", "Erro de Data", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Use ponto como separador decimal.", "Erro de Valor", JOptionPane.ERROR_MESSAGE);
            return;
        }

        addTransactionButton.setEnabled(false);

        SwingWorker<Transacao, Void> worker = new SwingWorker<Transacao, Void>() {
            private Exception error = null;

            @Override
            protected Transacao doInBackground() throws Exception {
                try {
                    return financeController.addTransaction(tipo, categoriaDesc, data, descricao, valor);
                } catch (Exception e) {
                    this.error = e;
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    if (error != null) {
                        JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                "Erro ao adicionar transação: " + error.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        error.printStackTrace();
                    } else {
                        Transacao savedTransacao = get();

                        if (savedTransacao != null) {
                            if (currentUser.getTransacoes() != null) {
                                boolean alreadyExists = currentUser.getTransacoes().stream()
                                        .anyMatch(t -> t.getId() != null && t.getId().equals(savedTransacao.getId()));
                                if (!alreadyExists) {
                                    currentUser.getTransacoes().add(savedTransacao);
                                }
                            } else {
                                currentUser.setTransacoes(new ArrayList<>());
                                currentUser.getTransacoes().add(savedTransacao);
                                System.err.println("Aviso: Lista de transações do usuário era nula e foi inicializada.");
                            }

                            updateFinanceData();
                            clearTransactionFormFields();

                            JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                    "Transação adicionada com sucesso!",
                                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                    "Erro inesperado ao adicionar transação (transação salva retornou null).",
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                            "Erro na execução da tarefa de adicionar transação: " + e.getMessage(),
                            "Erro Interno", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    addTransactionButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void filterTransactionsAction() {
        LocalDate startDate = null;
        LocalDate endDate = null;
        String tipo = (String) filterTypeCombo.getSelectedItem();
        String categoriaDesc = (String) managerCategoryCombo.getSelectedItem();

        try {
            String startDateStrRaw = startDateField.getText().replace("/", "").trim();
            if (!startDateStrRaw.contains("_") && startDateStrRaw.length() == 8) {
                startDate = LocalDate.parse(startDateField.getText(), viewDateFormatter);
            }
            String endDateStrRaw = endDateField.getText().replace("/", "").trim();
            if (!endDateStrRaw.contains("_") && endDateStrRaw.length() == 8) {
                endDate = LocalDate.parse(endDateField.getText(), viewDateFormatter);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido nos filtros. Use dd/MM/yyyy.", "Erro de Data", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tipo = "Todos".equalsIgnoreCase(tipo) ? null : tipo;
        categoriaDesc = "Todos".equalsIgnoreCase(categoriaDesc) ? null : categoriaDesc;

        List<Transacao> filteredTransactions = financeController.filterTransactions(startDate, endDate, tipo, categoriaDesc);
        updateTransactionList(filteredTransactions);
        updateSummary(filteredTransactions);
    }

    private void addCategoryActionWithWorker() {
        String novaCategoriaDesc = JOptionPane.showInputDialog(this, "Digite o nome da nova categoria:");
        if (novaCategoriaDesc == null || novaCategoriaDesc.trim().isEmpty()) {
            return;
        }

        final String trimmedDesc = novaCategoriaDesc.trim();
        addCategoryButton.setEnabled(false);

        SwingWorker<Categoria, Void> worker = new SwingWorker<Categoria, Void>() {
            private Exception error = null;

            @Override
            protected Categoria doInBackground() throws Exception {
                try {
                    return financeController.addCategory(trimmedDesc);
                } catch (Exception e) {
                    this.error = e;
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    if (error != null) {
                        JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                "Erro ao adicionar categoria: " + error.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        if (!(error.getMessage().toLowerCase().contains("já existe"))) {
                            error.printStackTrace();
                        }
                    } else {
                        Categoria savedCategoria = get();

                        if (savedCategoria != null) {
                            if (currentUser.getCategorias() != null) {
                                boolean alreadyExists = currentUser.getCategorias().stream()
                                        .anyMatch(cat -> cat.getId() != null && cat.getId().equals(savedCategoria.getId()));
                                if (!alreadyExists) {
                                    currentUser.getCategorias().add(savedCategoria);
                                }
                            } else {
                                currentUser.setCategorias(new ArrayList<>());
                                currentUser.getCategorias().add(savedCategoria);
                                System.err.println("Aviso: Lista de categorias do usuário era nula e foi inicializada.");
                            }

                            updateCategoryList();
                            updateCategoryComboBoxes();
                            updateManagerCategoryComboBox();

                            JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                    "Categoria \"" + savedCategoria.getDescricao() + "\" adicionada com sucesso!",
                                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                    "Erro inesperado ao adicionar categoria (categoria salva retornou null).",
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                            "Erro na execução da tarefa de adicionar categoria: " + e.getMessage(),
                            "Erro Interno", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    addCategoryButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void editCategoryAction() {
        int selectedIndex = categoryList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Categoria> categorias = currentUser.getCategorias();
        if (categorias == null || selectedIndex < 0 || selectedIndex >= categorias.size()) {
            JOptionPane.showMessageDialog(this, "Erro: Seleção inválida ou lista de categorias não carregada.", "Erro", JOptionPane.ERROR_MESSAGE);
            updateCategoryList();
            return;
        }

        Categoria categoriaSelecionada = categorias.get(selectedIndex);
        Long categoriaId = categoriaSelecionada.getId();
        String nomeAtual = categoriaSelecionada.getDescricao();

        String novoNome = JOptionPane.showInputDialog(this, "Digite o novo nome para a categoria \\\'" + nomeAtual + "\\\':", nomeAtual);

        if (novoNome != null && !novoNome.trim().isEmpty() && !novoNome.trim().equals(nomeAtual)) {
            try {
                financeController.editCategory(categoriaId, novoNome.trim());
                JOptionPane.showMessageDialog(this, "Categoria editada com sucesso!");
                categoriaSelecionada.setDescricao(novoNome.trim());
                updateCategoryList();
                updateCategoryComboBoxes();
                updateManagerCategoryComboBox();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao editar categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void deleteCategoryAction() {
        int selectedIndex = categoryList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Categoria> categorias = currentUser.getCategorias();
        if (categorias == null || selectedIndex < 0 || selectedIndex >= categorias.size()) {
            JOptionPane.showMessageDialog(this, "Erro: Seleção inválida ou lista de categorias não carregada.", "Erro", JOptionPane.ERROR_MESSAGE);
            updateCategoryList();
            return;
        }

        Categoria categoriaSelecionada = categorias.get(selectedIndex);
        Long categoriaId = categoriaSelecionada.getId();
        String nomeCategoria = categoriaSelecionada.getDescricao();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir a categoria \\\'" + nomeCategoria + "\\\'?\\n(Apenas categorias sem transações associadas podem ser excluídas)",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                financeController.deleteCategory(categoriaId);
                JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!");
                currentUser.getCategorias().remove(selectedIndex);
                updateCategoryList();
                updateCategoryComboBoxes();
                updateManagerCategoryComboBox();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                if (!ex.getMessage().toLowerCase().contains("transações associadas")) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void updateFinanceData() {
        List<Transacao> allTransactions = currentUser.getTransacoes();
        updateTransactionList(allTransactions);
        updateSummary(allTransactions);
    }

    private void updateCategoryList() {
        categoryListModel.clear();
        List<Categoria> categorias = currentUser.getCategorias();
        if (categorias != null) {
            categorias.sort((c1, c2) -> c1.getDescricao().compareToIgnoreCase(c2.getDescricao()));
            for (Categoria cat : categorias) {
                categoryListModel.addElement(cat.getDescricao());
            }
        }
    }

    private void updateCategoryComboBoxes() {
        categoryComboBox.removeAllItems();
        List<Categoria> categorias = currentUser.getCategorias();
        if (categorias != null) {
            for (Categoria cat : categorias) {
                categoryComboBox.addItem(cat.getDescricao());
            }
        }
    }

    private void updateManagerCategoryComboBox() {
        managerCategoryCombo.removeAllItems();
        managerCategoryCombo.addItem("Todos");
        List<Categoria> categorias = currentUser.getCategorias();
        if (categorias != null) {
            for (Categoria cat : categorias) {
                managerCategoryCombo.addItem(cat.getDescricao());
            }
        }
    }

    private void updateTransactionList(List<Transacao> transactions) {
        transactionListModel.clear();
        if (transactions != null) {
            transactions.sort((t1, t2) -> t2.getData().compareTo(t1.getData()));
            for (Transacao t : transactions) {
                String categoryName = (t.getCategoria() != null) ? t.getCategoria().getDescricao() : "N/A";
                transactionListModel.addElement(String.format("%s - %s (%s): R$ %.2f - %s",
                        t.getData().format(viewDateFormatter),
                        t.getTipo(),
                        categoryName,
                        t.getValor(),
                        t.getDescricao()));
            }
        }
    }

    private void updateSummary(List<Transacao> transactions) {
        FinanceController.FinancialSummary summary = financeController.calculateSummary(transactions);
        balanceLabel.setText(String.format("Saldo: R$ %.2f", summary.balance));
        incomeLabel.setText(String.format("Receitas: R$ %.2f", summary.totalIncome));
        expenseLabel.setText(String.format("Despesas: R$ %.2f", summary.totalExpense));
    }

    private void clearTransactionFormFields() {
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(categoryComboBox.getItemCount() > 0 ? 0 : -1);
        dateField.setValue(null);
        descriptionField.setText("");
        valueField.setText("");
    }
}
