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
import java.util.ArrayList; // Import ArrayList
import java.util.List;
import java.util.concurrent.ExecutionException;

// Tela Financeira com layout EXATAMENTE igual ao original, usando Controller
public class FinanceManagerScreen extends JFrame {
    // --- Componentes da UI (Declarações do original) ---
    private JTextField valueField, descriptionField;
    // Corrigido: dateField era JTextField no original, mas precisa ser JFormattedTextField pela máscara
    private JFormattedTextField dateField, startDateField, endDateField;
    private JComboBox<String> categoryComboBox, typeComboBox, managerCategoryCombo, filterTypeCombo;
    private JLabel balanceLabel, incomeLabel, expenseLabel;
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;
    // Corrigido: transactionList era JList<String> no original, mas precisa de Model
    private DefaultListModel<String> transactionListModel;
    private JList<String> transactionList;
    // Referências aos botões para desabilitar/habilitar durante operações
    private JButton addCategoryButton;
    private JButton addTransactionButton; // Adicionado como membro da classe

    // --- Atributos MVC ---
    private Usuario currentUser; // Mantido para exibir nome, etc.
    private FinanceController financeController; // Controller para lógica
    private final DateTimeFormatter viewDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FinanceManagerScreen(Usuario user) throws ParseException {
        this.currentUser = user; // Armazena o usuário passado pelo Login
        this.financeController = new FinanceController(user);
        // Pega dados atualizados, incluindo coleções inicializadas
        this.currentUser = financeController.getCurrentUser();

        if (this.currentUser == null) {
            JOptionPane.showMessageDialog(this, "Erro fatal ao carregar dados do usuário. Saindo.", "Erro", JOptionPane.ERROR_MESSAGE);
            // Garante que a tela de login seja exibida na EDT
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
            dispose();
            return;
        }
        // Garante que as listas não sejam nulas (boa prática)
        if (this.currentUser.getCategorias() == null) {
            this.currentUser.setCategorias(new ArrayList<>());
        }
        if (this.currentUser.getTransacoes() == null) {
            this.currentUser.setTransacoes(new ArrayList<>());
        }

        // --- Configurações básicas da janela (do original) ---
        setTitle("Gerenciador Financeiro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Construção da UI (Layout EXATO do original, lógica adaptada) ---
        setupUI();

        // --- Carregamento inicial dos dados via Controller (na EDT após UI construída) ---
        // Usar invokeLater para garantir que a UI esteja pronta antes de popular
        SwingUtilities.invokeLater(() -> {
            updateFinanceData();
            updateCategoryList(); // Usa currentUser já carregado
            updateCategoryComboBoxes(); // Usa currentUser já carregado
            updateManagerCategoryComboBox(); // Usa currentUser já carregado
        });
    }

    // Método para criar o MaskFormatter (do original)
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

    // Configura a UI principal (estrutura EXATA do original)
    private void setupUI() {
        // --- Menu Bar (original) ---
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("Usuário: " + currentUser.getNome());
        JMenuItem logoutItem = new JMenuItem("Sair");
        logoutItem.addActionListener(e -> {
            this.dispose();
            // Garante que a tela de login seja exibida na EDT
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        });
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        // --- Painel Principal (original) ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORMULÁRIO DE CADASTRO DE TRANSACOES (Layout original) ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10)); // 6 linhas, 2 colunas
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastro de Transações"));

        formPanel.add(new JLabel("Tipo:"));
        typeComboBox = new JComboBox<>(new String[]{"Receita", "Despesa"});
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Categoria:"));
        categoryComboBox = new JComboBox<>(); // Populado depois
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Data:")); // Label original sem formato
        MaskFormatter originalDateFormatter = createDateFormatter(); // Usa a máscara
        dateField = new JFormattedTextField(originalDateFormatter);
        dateField.setColumns(10);
        formPanel.add(dateField);

        formPanel.add(new JLabel("Descrição:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Valor:"));
        valueField = new JTextField();
        formPanel.add(valueField);

        // Botão Adicionar Transação (layout original)
        // Atribui à variável de instância
        addTransactionButton = new JButton("Adicionar Transação");
        addTransactionButton.setPreferredSize(new Dimension(10, 30)); // Tamanho original
        // --- ActionListener modificado para usar SwingWorker ---
        addTransactionButton.addActionListener(e -> addTransactionActionWithWorker());
        // Adiciona o botão na última linha, primeira coluna (como no original)
        formPanel.add(addTransactionButton);
        formPanel.add(new JLabel()); // Placeholder na última linha, segunda coluna

        // --- PAINÉIS DE HISTÓRICO E RESUMO (Layout original) ---
        // Painel intermediário historyAndSummaryPanel removido para seguir estrutura original

        // --- Painel Histórico (original) ---
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        // --- Painel Filtro (layout original com GridBagLayout) ---
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
        gbc.gridx = 3; managerCategoryCombo = new JComboBox<>(); // Populado depois
        filterPanel.add(managerCategoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.CENTER; // Configuração exata do original
        JButton filterButton = new JButton("Aplicar Filtros");
        // --- ActionListener adaptado para usar Controller ---
        filterButton.addActionListener(e -> filterTransactionsAction());
        filterPanel.add(filterButton, gbc);

        historyPanel.add(filterPanel, BorderLayout.NORTH);

        // --- Lista de Transações (original) ---
        transactionListModel = new DefaultListModel<>();
        transactionList = new JList<>(transactionListModel);
        JScrollPane scrollPane = new JScrollPane(transactionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Painel Resumo (original) ---
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 linhas, 1 coluna
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        balanceLabel = new JLabel("Saldo: R$ 0.00"); // Texto inicial
        incomeLabel = new JLabel("Receitas: R$ 0.00");
        expenseLabel = new JLabel("Despesas: R$ 0.00");
        summaryPanel.add(balanceLabel);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        // Adições ao painel intermediário removidas

        // --- PAINEL DE CATEGORIAS (Layout original) ---
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Categorias"));
        // Tamanho preferencial removido para igualar ao original

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryPanel.add(categoryScrollPane, BorderLayout.CENTER);

        // --- Painel de Botões de Categoria (FlowLayout original) ---
        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // Atribui o botão à variável de instância
        addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        // --- ActionListeners adaptados para usar Controller (AddCategoryAction modificado) ---
        addCategoryButton.addActionListener(e -> addCategoryActionWithWorker()); // Chama o método com SwingWorker
        editCategoryButton.addActionListener(e -> editCategoryAction());
        deleteCategoryButton.addActionListener(e -> deleteCategoryAction());

        categoryButtonPanel.add(addCategoryButton);
        categoryButtonPanel.add(editCategoryButton);
        categoryButtonPanel.add(deleteCategoryButton);

        categoryPanel.add(categoryButtonPanel, BorderLayout.SOUTH);

        // --- Cria painéis de agrupamento para layout correto (baseado na imagem) ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10)); // Painel para agrupar form e categorias verticalmente
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(categoryPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 10)); // Painel para agrupar histórico e resumo verticalmente
        rightPanel.add(historyPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);

        // --- Altera layout do mainPanel e adiciona painéis agrupados ---
        mainPanel.setLayout(new GridLayout(1, 2, 10, 10)); // Layout 1 linha, 2 colunas com espaçamento
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    // --- Métodos de Ação (adaptados para usar Controller) ---

    // --- NOVA AÇÃO PARA ADICIONAR TRANSAÇÃO USANDO SWINGWORKER (SEGUINDO LÓGICA DO PASTED_CONTENT) ---
    private void addTransactionActionWithWorker() {
        // Validação inicial na EDT
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

        addTransactionButton.setEnabled(false); // Desabilita o botão

        // Cria e executa o SwingWorker
        SwingWorker<Transacao, Void> worker = new SwingWorker<Transacao, Void>() {
            private Exception error = null;

            @Override
            protected Transacao doInBackground() throws Exception {
                try {
                    // Chama o método do controller que salva no banco
                    // O controller NÃO deve mais atualizar seu currentUser internamente aqui
                    // para seguir a lógica do pasted_content
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
                        Transacao savedTransacao = get(); // Pode lançar ExecutionException ou InterruptedException

                        if (savedTransacao != null) {
                            // Sucesso!
                            // ** IMPORTANTE: Adiciona manualmente à lista em memória (lógica do pasted_content) **
                            if (currentUser.getTransacoes() != null) {
                                // Verifica se já não está na lista (segurança extra)
                                boolean alreadyExists = currentUser.getTransacoes().stream()
                                        .anyMatch(t -> t.getId() != null && t.getId().equals(savedTransacao.getId()));
                                if (!alreadyExists) {
                                    currentUser.getTransacoes().add(savedTransacao);
                                }
                            } else {
                                // Se a lista for nula, inicializa e adiciona
                                currentUser.setTransacoes(new ArrayList<>());
                                currentUser.getTransacoes().add(savedTransacao);
                                System.err.println("Aviso: Lista de transações do usuário era nula e foi inicializada.");
                            }

                            // Agora, atualiza a UI usando a lista em memória modificada
                            updateFinanceData(); // Atualiza lista de transações e resumo
                            clearTransactionFormFields(); // Limpa o formulário

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
                    addTransactionButton.setEnabled(true); // Reabilita o botão SEMPRE
                }
            }
        };

        worker.execute();
    }

    // Ação original de adicionar transação (mantida para referência, mas não usada)
    /*
    private void addTransactionAction() {
        ...
    }
    */

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

        // Filtragem pode ser demorada, idealmente usaria SwingWorker
        // Para simplificar, mantemos na EDT por enquanto.
        List<Transacao> filteredTransactions = financeController.filterTransactions(startDate, endDate, tipo, categoriaDesc);
        updateTransactionList(filteredTransactions);
        updateSummary(filteredTransactions);
    }

    // --- NOVA AÇÃO PARA ADICIONAR CATEGORIA USANDO SWINGWORKER (SEGUINDO LÓGICA DO PASTED_CONTENT) ---
    private void addCategoryActionWithWorker() {
        String novaCategoriaDesc = JOptionPane.showInputDialog(this, "Digite o nome da nova categoria:");
        if (novaCategoriaDesc == null || novaCategoriaDesc.trim().isEmpty()) {
            return; // Usuário cancelou ou não digitou nada
        }

        final String trimmedDesc = novaCategoriaDesc.trim();
        addCategoryButton.setEnabled(false); // Desabilita o botão

        // Cria e executa o SwingWorker
        SwingWorker<Categoria, Void> worker = new SwingWorker<Categoria, Void>() {
            private Exception error = null; // Para armazenar exceção da thread de fundo

            @Override
            protected Categoria doInBackground() throws Exception {
                try {
                    // Chama o método do controller que salva no banco
                    // Este método já verifica se a categoria existe
                    return financeController.addCategory(trimmedDesc);
                } catch (Exception e) {
                    this.error = e; // Armazena a exceção para tratar no \\\'done\\\'
                    return null; // Retorna null em caso de erro
                }
            }

            @Override
            protected void done() {
                try {
                    if (error != null) {
                        // Se houve exceção no doInBackground, mostra a mensagem de erro
                        JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                "Erro ao adicionar categoria: " + error.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        if (!(error.getMessage().toLowerCase().contains("já existe"))) {
                            // Loga o stack trace se não for erro de duplicidade
                            error.printStackTrace();
                        }
                    } else {
                        // Tenta obter o resultado (a Categoria salva)
                        Categoria savedCategoria = get(); // Pode lançar ExecutionException ou InterruptedException

                        if (savedCategoria != null) {
                            // Sucesso!
                            // ** IMPORTANTE: Adiciona manualmente à lista em memória (lógica do pasted_content) **
                            if (currentUser.getCategorias() != null) {
                                // Verifica se já não está na lista (segurança extra)
                                boolean alreadyExists = currentUser.getCategorias().stream()
                                        .anyMatch(cat -> cat.getId() != null && cat.getId().equals(savedCategoria.getId()));
                                if (!alreadyExists) {
                                    currentUser.getCategorias().add(savedCategoria);
                                }
                            } else {
                                // Se a lista for nula, inicializa e adiciona
                                currentUser.setCategorias(new ArrayList<>());
                                currentUser.getCategorias().add(savedCategoria);
                                System.err.println("Aviso: Lista de categorias do usuário era nula e foi inicializada.");
                            }

                            // Atualiza a UI com a lista modificada
                            updateCategoryList(); // Atualiza a JList
                            updateCategoryComboBoxes(); // Atualiza os ComboBoxes
                            updateManagerCategoryComboBox(); // Atualiza o ComboBox de filtro

                            JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                    "Categoria \"" + savedCategoria.getDescricao() + "\" adicionada com sucesso!",
                                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            // Caso get() retorne null sem exceção (não deveria acontecer com a lógica atual)
                            JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                                    "Erro inesperado ao adicionar categoria (categoria salva retornou null).",
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    // Erros relacionados à execução do SwingWorker ou cancelamento
                    JOptionPane.showMessageDialog(FinanceManagerScreen.this,
                            "Erro na execução da tarefa de adicionar categoria: " + e.getMessage(),
                            "Erro Interno", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    addCategoryButton.setEnabled(true); // Reabilita o botão SEMPRE
                }
            }
        };

        worker.execute(); // Inicia a execução do SwingWorker
    }

    // Ação original de adicionar categoria (mantida para referência, mas não usada)
    /*
    private void addCategoryAction() {
        ...
    }
    */

    private void editCategoryAction() {
        int selectedIndex = categoryList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pega a categoria diretamente da lista do currentUser para garantir consistência
        List<Categoria> categorias = currentUser.getCategorias(); // Assume que está atualizada
        if (categorias == null || selectedIndex < 0 || selectedIndex >= categorias.size()) {
            JOptionPane.showMessageDialog(this, "Erro: Seleção inválida ou lista de categorias não carregada.", "Erro", JOptionPane.ERROR_MESSAGE);
            updateCategoryList(); // Tenta recarregar a lista visual
            return;
        }

        Categoria categoriaSelecionada = categorias.get(selectedIndex);
        Long categoriaId = categoriaSelecionada.getId();
        String nomeAtual = categoriaSelecionada.getDescricao();

        String novoNome = JOptionPane.showInputDialog(this, "Digite o novo nome para a categoria \\\'" + nomeAtual + "\\\':", nomeAtual);

        if (novoNome != null && !novoNome.trim().isEmpty() && !novoNome.trim().equals(nomeAtual)) {
            // Edição também pode ser demorada, idealmente usaria SwingWorker
            // Para simplificar, mantemos na EDT por enquanto.
            try {
                financeController.editCategory(categoriaId, novoNome.trim());
                JOptionPane.showMessageDialog(this, "Categoria editada com sucesso!");
                // ** IMPORTANTE: Atualiza a referência local do currentUser **
                // Não precisa mais, pois a edição agora segue a lógica de adicionar manualmente
                // currentUser = financeController.getCurrentUser();
                // Atualiza o objeto na lista em memória
                categoriaSelecionada.setDescricao(novoNome.trim());
                // Atualiza a UI
                updateCategoryList(); // Atualiza a JList
                updateCategoryComboBoxes(); // Atualiza os ComboBoxes
                updateManagerCategoryComboBox(); // Atualiza o ComboBox de filtro
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

        List<Categoria> categorias = currentUser.getCategorias(); // Assume que está atualizada
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
            // Exclusão também pode ser demorada, idealmente usaria SwingWorker
            // Para simplificar, mantemos na EDT por enquanto.
            try {
                financeController.deleteCategory(categoriaId);
                JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!");
                // ** IMPORTANTE: Remove da lista local E atualiza a UI **
                currentUser.getCategorias().remove(selectedIndex);
                // Atualiza a UI
                updateCategoryList();
                updateCategoryComboBoxes();
                updateManagerCategoryComboBox();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                // Não printa stack trace para erros esperados como "transações associadas"
                if (!ex.getMessage().toLowerCase().contains("transações associadas")) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // --- Métodos de Atualização da UI (adaptados para usar Controller e dados do currentUser) ---

    // Atualiza todos os dados financeiros (resumo e lista de transações inicial)
    // Agora usa a referência local atualizada de currentUser
    private void updateFinanceData() {
        List<Transacao> allTransactions = currentUser.getTransacoes(); // Pega da instância local atualizada
        updateTransactionList(allTransactions);
        updateSummary(allTransactions);
    }

    // Atualiza a JList de categorias
    // Agora usa a referência local atualizada de currentUser
    private void updateCategoryList() {
        categoryListModel.clear();
        List<Categoria> categorias = currentUser.getCategorias(); // Pega da instância local atualizada
        if (categorias != null) {
            // Ordena alfabeticamente para melhor visualização
            categorias.sort((c1, c2) -> c1.getDescricao().compareToIgnoreCase(c2.getDescricao()));
            for (Categoria cat : categorias) {
                categoryListModel.addElement(cat.getDescricao());
            }
        }
    }

    // Atualiza os ComboBoxes de categoria (formulário e filtro)
    // Agora usa a referência local atualizada de currentUser
    private void updateCategoryComboBoxes() {
        categoryComboBox.removeAllItems();
        List<Categoria> categorias = currentUser.getCategorias(); // Pega da instância local atualizada
        if (categorias != null) {
            // Mantém a ordem (pode ser a ordem de adição ou a ordenada da lista)
            for (Categoria cat : categorias) {
                categoryComboBox.addItem(cat.getDescricao());
            }
        }
    }

    // Atualiza o ComboBox de categoria no painel de filtro (adiciona "Todos")
    // Agora usa a referência local atualizada de currentUser
    private void updateManagerCategoryComboBox() {
        managerCategoryCombo.removeAllItems();
        managerCategoryCombo.addItem("Todos"); // Opção padrão
        List<Categoria> categorias = currentUser.getCategorias(); // Pega da instância local atualizada
        if (categorias != null) {
            // Mantém a ordem
            for (Categoria cat : categorias) {
                managerCategoryCombo.addItem(cat.getDescricao());
            }
        }
    }

    // Atualiza a JList de transações com base na lista fornecida
    private void updateTransactionList(List<Transacao> transactions) {
        transactionListModel.clear();
        if (transactions != null) {
            transactions.sort((t1, t2) -> t2.getData().compareTo(t1.getData())); // Ordena por data descendente
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

    // Atualiza o painel de resumo com base na lista de transações fornecida
    private void updateSummary(List<Transacao> transactions) {
        FinanceController.FinancialSummary summary = financeController.calculateSummary(transactions);
        balanceLabel.setText(String.format("Saldo: R$ %.2f", summary.balance));
        incomeLabel.setText(String.format("Receitas: R$ %.2f", summary.totalIncome));
        expenseLabel.setText(String.format("Despesas: R$ %.2f", summary.totalExpense));
    }

    // Limpa os campos do formulário de transação
    private void clearTransactionFormFields() {
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(categoryComboBox.getItemCount() > 0 ? 0 : -1); // Seleciona o primeiro se houver
        dateField.setValue(null); // Limpa campo formatado
        descriptionField.setText("");
        valueField.setText("");
    }
}
