package org.example;

import java.util.ArrayList;
import java.util.List;

public class BancoDados {
    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Categoria> categorias = new ArrayList<>();

    public static void adicionarUsuario(Usuario usuario) {
        if (!usuarios.contains(usuario)) {
            usuarios.add(usuario);
        }
    }

    public static Usuario autenticarUsuario(String email, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equals(email) && usuario.verificarSenha(senha)) {
                return usuario;
            }
        }
        return null;  // Retorna null se o login falhar
    }

    public static List<Usuario> getUsuarios() {
        return usuarios;
    }

    public static void adicionarCategoria(Categoria categoria) {
        if (!categorias.contains(categoria)) {
            categorias.add(categoria);
        }
    }

    public static List<Categoria> getCategorias() {
        return categorias;
    }
}
