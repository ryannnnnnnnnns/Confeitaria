package com.ifsp.projeto.service;

import com.ifsp.projeto.model.Usuario;
import com.ifsp.projeto.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço para gerenciar a lógica de negócio relacionada a usuários.
 * Responsável pelo cadastro, autenticação, atualização de dados (senha, login),
 * e gerenciamento de permissões de usuários.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Busca um usuário pelo seu nome de login.
     * @param login O nome de login do usuário.
     * @return Um {@link Optional} contendo o usuário, ou vazio se não encontrado.
     */
    public Optional<Usuario> findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    /**
     * Registra um novo usuário no sistema.
     * O primeiro usuário registrado se torna ADMIN e é ativado. Os demais são criados como USER e desativados, aguardando aprovação.
     * @param usuario A entidade {@link Usuario} com os dados do novo usuário.
     * @param confirmarSenha A confirmação da senha, para validação.
     * @return O usuário salvo com a senha criptografada.
     * @throws IllegalArgumentException Se as senhas não coincidirem ou o login já existir.
     */
    @Transactional
    public Usuario salvarUsuario(Usuario usuario, String confirmarSenha) {
        if (!usuario.getSenha().equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }
        if (usuarioRepository.findByLogin(usuario.getLogin()).isPresent()) {
            throw new IllegalArgumentException("Login já existe.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        if (usuarioRepository.count() == 0) {
            usuario.setRole("ADMIN");
            usuario.setEnabled(true);
        } else {
            usuario.setRole("USER");
            usuario.setEnabled(false);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Permite que um usuário autenticado redefina sua própria senha.
     * @param login O login do usuário.
     * @param senhaAntiga A senha atual, para verificação.
     * @param novaSenha A nova senha desejada.
     * @param confirmarNovaSenha A confirmação da nova senha.
     * @throws IllegalArgumentException Se a senha antiga estiver incorreta ou as novas senhas não coincidirem.
     */
    @Transactional
    public void redefinirSenha(String login, String senhaAntiga, String novaSenha, String confirmarNovaSenha) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(senhaAntiga, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha antiga incorreta.");
        }

        if (!novaSenha.equals(confirmarNovaSenha)) {
            throw new IllegalArgumentException("As novas senhas não coincidem.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    /**
     * Permite que um usuário autenticado altere seu nome de login.
     * @param login O login do usuário que está realizando a operação.
     * @param loginAtual Confirmação do login atual do usuário.
     * @param novoLogin O novo login desejado.
     * @param senha A senha atual do usuário, para verificação.
     * @throws IllegalArgumentException Se os dados de entrada estiverem incorretos ou o novo login já estiver em uso.
     */
    @Transactional
    public void redefinirLogin(String login, String loginAtual, String novoLogin, String senha) {
        if (!login.equals(loginAtual)) {
            throw new IllegalArgumentException("Nome de usuário atual incorreto.");
        }
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta.");
        }

        if (usuarioRepository.findByLogin(novoLogin).isPresent()) {
            throw new IllegalArgumentException("O novo nome de usuário já está em uso.");
        }

        usuario.setLogin(novoLogin);
        usuarioRepository.save(usuario);
    }

    /**
     * Permite que um usuário autenticado altere sua pergunta e resposta secretas.
     * @param login O login do usuário.
     * @param senha A senha atual do usuário, para verificação.
     * @param novaPerguntaSecreta A nova pergunta secreta.
     * @param novaRespostaSecreta A nova resposta secreta.
     * @throws IllegalArgumentException Se o usuário não for encontrado ou a senha estiver incorreta.
     */
    @Transactional
    public void redefinirPergunta(String login, String senha, String novaPerguntaSecreta, String novaRespostaSecreta) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta.");
        }

        usuario.setPerguntaSecreta(novaPerguntaSecreta);
        usuario.setRespostaSecreta(novaRespostaSecreta);
        usuarioRepository.save(usuario);
    }

    /**
     * Inicia o processo de 'esqueci minha senha' validando a existência do usuário.
     * @param login O login do usuário que esqueceu a senha.
     * @throws IllegalArgumentException Se o usuário não for encontrado.
     */
    @Transactional
    public void processEsqueciSenha(String login) {
        usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    /**
     * Verifica se a resposta secreta fornecida corresponde à do usuário.
     * @param login O login do usuário.
     * @param respostaSecreta A resposta fornecida pelo usuário.
     * @throws IllegalArgumentException Se a resposta estiver incorreta ou o usuário não for encontrado.
     */
    @Transactional
    public void verificarResposta(String login, String respostaSecreta) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!usuario.getRespostaSecreta().equalsIgnoreCase(respostaSecreta)) {
            throw new IllegalArgumentException("Resposta secreta incorreta.");
        }
    }

    /**
     * Define uma nova senha para o usuário, geralmente ao final do fluxo 'esqueci minha senha'.
     * @param login O login do usuário.
     * @param novaSenha A nova senha.
     * @param confirmarNovaSenha A confirmação da nova senha.
     * @throws IllegalArgumentException Se as senhas não coincidirem ou o usuário não for encontrado.
     */
    @Transactional
    public void salvarNovaSenha(String login, String novaSenha, String confirmarNovaSenha) {
        if (!novaSenha.equals(confirmarNovaSenha)) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    /**
     * Busca todos os usuários cadastrados no sistema.
     * @return Uma lista de todos os usuários.
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Aprova o cadastro de um usuário, ativando sua conta.
     * @param id O ID do usuário a ser aprovado.
     */
    @Transactional
    public void aprovarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        usuario.setEnabled(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Reprova o cadastro de um usuário, desativando sua conta.
     * @param id O ID do usuário a ser reprovado.
     */
    @Transactional
    public void reprovarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        usuario.setEnabled(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Exclui um usuário do sistema.
     * @param id O ID do usuário a ser excluído.
     */
    @Transactional
    public void excluirUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}