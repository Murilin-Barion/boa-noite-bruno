package org.example.model.dao;

import org.example.model.Categoria;
import org.example.model.Transacao;
import org.example.model.Usuario;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoDAO extends GenericDAO<Transacao, Long> {
    List<Transacao> findByUsuario(Usuario usuario);

    List<Transacao> findByCriteria(Usuario usuario, LocalDate startDate, LocalDate endDate, String tipo, Categoria categoria);

    long countByCategoria(Categoria categoria);
}

