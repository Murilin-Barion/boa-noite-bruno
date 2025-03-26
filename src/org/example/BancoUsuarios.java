package org.example;

import java.util.ArrayList;
import java.util.List;

public class BancoUsuarios {
    private static List<Usuario> usuarios = new ArrayList<>();


    public static void adicionarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public static Usuario autenticar(String email, String senha) {
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
}
