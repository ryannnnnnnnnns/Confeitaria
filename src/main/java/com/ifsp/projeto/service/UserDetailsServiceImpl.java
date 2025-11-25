package com.ifsp.projeto.service;

import com.ifsp.projeto.model.Usuario;
import com.ifsp.projeto.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Implementação do serviço {@link UserDetailsService} do Spring Security.
 * Esta classe é responsável por carregar os detalhes de um usuário (como senha e permissões)
 * a partir do banco de dados, permitindo que o Spring Security realize a autenticação.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Localiza um usuário pelo seu nome de login (username).
     * Este método é chamado pelo Spring Security durante o processo de autenticação. Ele busca o usuário no repositório,
     * verifica se a conta está ativa e converte as roles do usuário para as {@link GrantedAuthority} do Spring.
     *
     * @param username O nome de login fornecido pelo usuário na tela de login.
     * @return Um objeto {@link UserDetails} contendo os dados do usuário para o Spring Security.
     * @throws UsernameNotFoundException Se o usuário não for encontrado no banco de dados ou se a conta estiver desativada.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o login: " + username));

        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("User is disabled");
        }

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()));

        return new User(usuario.getLogin(), usuario.getSenha(), authorities);
    }
}
