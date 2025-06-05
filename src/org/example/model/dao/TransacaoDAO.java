package org.example.model.dao;

import org.example.model.Categoria;
import org.example.model.Transacao;
import org.example.model.Usuario;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoDAO extends GenericDAO<Transacao, Long> {
    List<Transacao> findByUsuario(Usuario usuario);

    // Método para buscar transações com critérios (usado pelo controller)
    List<Transacao> findByCriteria(Usuario usuario, LocalDate startDate, LocalDate endDate, String tipo, Categoria categoria);

    // Método para contar transações por categoria (usado pelo controller antes de excluir categoria)
    long countByCategoria(Categoria categoria);

    // Método antigo que pode ser removido ou mantido se usado em outro lugar (removido para evitar confusão)
    // List<Transacao> findByUsuarioAndFilters(Usuario usuario, LocalDate startDate, LocalDate endDate, String type, String categoryDesc);
}

