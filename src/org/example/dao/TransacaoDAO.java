package org.example.dao;

import org.example.Transacao;
import org.example.Usuario;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoDAO extends GenericDAO<Transacao, Long> {
    List<Transacao> findByUsuario(Usuario usuario);
    List<Transacao> findByUsuarioAndFilters(Usuario usuario, LocalDate startDate, LocalDate endDate, String type, String categoryDesc);
}