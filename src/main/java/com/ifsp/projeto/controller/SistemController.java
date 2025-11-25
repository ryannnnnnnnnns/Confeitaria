package com.ifsp.projeto.controller;

import com.ifsp.projeto.controller.dto.*;
import com.ifsp.projeto.model.*;
import com.ifsp.projeto.repository.MateriaPrimaRepository;
import com.ifsp.projeto.repository.ProdutoRepository;
import com.ifsp.projeto.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Controlador principal da aplicação.
 * Gerencia todas as requisições web para as diferentes funcionalidades do sistema, como login, home,
 * gerenciamento de estoque, produtos, produção, vendas, pedidos, orçamentos e configurações de usuário.
 * Atua como o ponto de entrada para a interação do usuário com a interface web.
 */
@Controller
public class SistemController {

    private static final Logger log = LoggerFactory.getLogger(SistemController.class);

    private final ProdutoService produtoService;
    private final ProducaoService producaoService;
    private final UsuarioService usuarioService;
    private final VendaService vendaService;
    private final MateriaPrimaService materiaPrimaService;
    private final PedidoService pedidoService;
    private final OrcamentoService orcamentoService;
    private final MateriaPrimaRepository materiaPrimaRepository;
    private final ProdutoRepository produtoRepository;


    public SistemController(ProdutoService produtoService, ProducaoService producaoService, UsuarioService usuarioService, VendaService vendaService, MateriaPrimaService materiaPrimaService, PedidoService pedidoService, OrcamentoService orcamentoService, MateriaPrimaRepository materiaPrimaRepository, ProdutoRepository produtoRepository) {
        this.produtoService = produtoService;
        this.producaoService = producaoService;
        this.usuarioService = usuarioService;
        this.vendaService = vendaService;
        this.materiaPrimaService = materiaPrimaService;
        this.pedidoService = pedidoService;
        this.orcamentoService = orcamentoService;
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Redireciona a rota raiz ("/") para a página de login.
     * @return Redirecionamento para "/login".
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    /**
     * Exibe a página de login.
     * @return O template "telaLogin".
     */
    @GetMapping("/login")
    public String login() {
        return "telaLogin";
    }

    /**
     * Exibe a página inicial (home) após o login.
     * @param model O modelo para adicionar atributos para a view.
     * @return O template "home".
     */
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("materiasPrimasComEstoqueBaixo", materiaPrimaService.findLowStock());
        model.addAttribute("upcomingPedidos", pedidoService.findUpcomingPedidos());
        return "home";
    }

    /**
     * Exibe a tabela de estoque de matérias-primas, com opções de filtro e ordenação.
     * @param model O modelo para a view.
     * @param nome Filtro opcional por nome da matéria-prima.
     * @param unidade Filtro opcional por unidade de medida.
     * @param sort Campo opcional para ordenação.
     * @param order Ordem opcional da ordenação (asc/desc).
     * @return O template "tabelaEstoque".
     */
    @GetMapping("/estoque")
    public String estoque(Model model,
                        @RequestParam(required = false) String nome,
                        @RequestParam(required = false) String unidade,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String order) {
        model.addAttribute("materiasPrimas", materiaPrimaService.findAllWithAlert(nome, unidade, sort, order));
        return "tabelaEstoque";
    }

    /**
     * Exibe o relatório de vendas.
     * @param model O modelo para a view.
     * @return O template "relatorioVendas".
     */
    @GetMapping("/vendas")
    public String vendas(Model model) {
        List<Venda> vendas = vendaService.findAllWithDetails();
        int totalQuantidade = vendas.stream().mapToInt(Venda::getQuantidade).sum();
        double totalValor = vendas.stream().mapToDouble(Venda::getValorVenda).sum();

        model.addAttribute("vendas", vendas);
        model.addAttribute("dataInicio", null);
        model.addAttribute("dataFim", null);
        model.addAttribute("totalQuantidade", totalQuantidade);
        model.addAttribute("totalValor", totalValor);
        return "relatorioVendas";
    }

    /**
     * Exibe o formulário para dar entrada de estoque em uma matéria-prima existente.
     * @param model O modelo para a view.
     * @return O template "entradaMP".
     */
    @GetMapping("/entradaMP")
    public String entradaMP(Model model) {
        model.addAttribute("materiaPrima", new MateriaPrima());
        return "entradaMP";
    }

    /**
     * Processa a adição de estoque de uma matéria-prima.
     * @param materiaPrima Objeto com os dados da entrada de estoque.
     * @param model O modelo para a view.
     * @return Redireciona para a tela de estoque ou retorna ao formulário em caso de erro.
     */
    @PostMapping("/adicionarMP")
    public String adicionarMP(MateriaPrima materiaPrima, Model model) {
        try {
            materiaPrimaService.adicionarMP(materiaPrima);
            return "redirect:/estoque";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrima", new MateriaPrima());
            return "entradaMP";
        }
    }

    /**
     * Exibe o formulário para cadastrar uma nova matéria-prima.
     * @param model O modelo para a view.
     * @return O template "formularioMP".
     */
    @GetMapping("/formularioMP")
    public String formularioMP(Model model) {
        model.addAttribute("materiaPrima", new MateriaPrima());
        model.addAttribute("materiasPrimas", materiaPrimaService.findAll());
        return "formularioMP";
    }

    /**
     * Salva uma nova matéria-prima ou atualiza uma existente.
     * @param materiaPrima A matéria-prima a ser salva.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a tela de estoque.
     */
    @PostMapping("/salvarMP")
    public String salvarMP(MateriaPrima materiaPrima, RedirectAttributes redirectAttributes) {
        try {
            materiaPrimaService.salvarMP(materiaPrima);
            redirectAttributes.addFlashAttribute("success", "Matéria-prima salva com sucesso!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/estoque";
    }

    /**
     * Exibe o formulário de matéria-prima preenchido para edição.
     * @param id O ID da matéria-prima a ser editada.
     * @param model O modelo para a view.
     * @return O template "formularioMP".
     */
    @GetMapping("/editarMP/{id}")
    public String editarMP(@PathVariable("id") Long id, Model model) {
        Optional<MateriaPrima> materiaPrimaOpt = materiaPrimaService.findById(id);
        if (materiaPrimaOpt.isPresent()) {
            model.addAttribute("materiaPrima", materiaPrimaOpt.get());
            return "formularioMP";
        }
        else {
            return "redirect:/estoque";
        }
    }

    /**
     * Exclui uma matéria-prima.
     * @param id O ID da matéria-prima a ser excluída.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a tela de estoque.
     */
    @PostMapping("/excluirMP/{id}")
    public String excluirMP(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            materiaPrimaService.excluirMP(id);
            redirectAttributes.addFlashAttribute("success", "Matéria-prima excluída com sucesso.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/estoque";
    }

    /**
     * Exibe o formulário para cadastrar ou editar um produto.
     * @param model O modelo para a view.
     * @return O template "formularioProduto".
     */
    @GetMapping("/formulario-produto")
    public String formularioProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "formularioProduto";
    }

    /**
     * Exibe a lista de todos os produtos cadastrados.
     * @param model O modelo para a view.
     * @return O template "listaProdutos".
     */
    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        model.addAttribute("produtos", produtoService.findAll());
        return "listaProdutos";
    }

    /**
     * Exibe a página de detalhes de um produto, incluindo sua composição e custos.
     * @param id O ID do produto.
     * @param model O modelo para a view.
     * @return O template "detalhesProduto".
     */
    @GetMapping("/produto/{id}")
    public String detalharProduto(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produtoOpt = produtoService.findByIdWithIngredientes(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();

            List<IngredienteDetalheDTO> ingredientesDetalhe = new ArrayList<>();
            for (Ingrediente ingrediente : produto.getIngredientes()) {
                if (ingrediente.getMateriaPrima() != null && ingrediente.getQuantidade() != null) {
                    double valorUnitario = ingrediente.getMateriaPrima().getValor();
                    double custoIngrediente = ingrediente.getQuantidade() * valorUnitario;

                    ingredientesDetalhe.add(new IngredienteDetalheDTO(
                        ingrediente.getMateriaPrima().getNome(),
                        ingrediente.getQuantidade(),
                        ingrediente.getMateriaPrima().getUnidade(),
                        valorUnitario,
                        custoIngrediente
                    ));
                }
            }

            model.addAttribute("produto", produto);
            model.addAttribute("ingredientesDetalhe", ingredientesDetalhe);
            return "detalhesProduto";
        }
        return "redirect:/produtos?error=notfound";
    }

    /**
     * Recalcula e atualiza o preço de um produto com base no custo atual dos ingredientes.
     * @param id O ID do produto.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de detalhes do produto.
     */
    @PostMapping("/produto/atualizar-preco/{id}")
    public String atualizarPrecoProduto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.atualizarPrecoProduto(id);
            redirectAttributes.addFlashAttribute("success", "Preço do produto atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/produtos";
        }
        return "redirect:/produto/" + id;
    }

    /**
     * Exibe o formulário de produto preenchido para edição.
     * @param id O ID do produto a ser editado.
     * @param model O modelo para a view.
     * @return O template "formularioProduto".
     */
    @GetMapping("/editar-produto/{id}")
    public String editarProduto(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produtoOpt = produtoService.findByIdWithIngredientes(id);
        if (produtoOpt.isPresent()) {
            model.addAttribute("produto", produtoOpt.get());
            return "formularioProduto";
        }
        else {
            return "redirect:/produtos";
        }
    }

    /**
     * Exclui um produto e todos os seus dados associados.
     * @param id O ID do produto a ser excluído.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de produtos.
     */
    @PostMapping("/excluir-produto/{id}")
    public String excluirProduto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.excluirProduto(id);
            redirectAttributes.addFlashAttribute("success", "Produto e todos os dados associados (produções, vendas, pedidos e orçamentos) foram excluídos com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produtos";
    }

    /**
     * Salva um novo produto ou atualiza um existente, junto com sua lista de ingredientes.
     * @param produto O produto a ser salvo.
     * @param ingredientesIds Lista de IDs das matérias-primas (ingredientes).
     * @param quantidades Lista de quantidades de cada ingrediente.
     * @return Redirecionamento para a lista de produtos.
     */
    @PostMapping("/salvar-produto")
    public String salvarProduto(Produto produto, @RequestParam(value = "ingredientesIds", required = false) List<Long> ingredientesIds, @RequestParam(value = "quantidades", required = false) List<Double> quantidades) {
        log.info("Recebendo requisição para salvar produto: {}", produto.getNome().replaceAll("[\n\r]", "_"));
        log.info("Ingredientes IDs recebidos: {}", ingredientesIds);
        log.info("Quantidades recebidas: {}", quantidades);

        produtoService.salvarProduto(produto, ingredientesIds, quantidades);

        log.info("Produto salvo com sucesso!");
        return "redirect:/produtos";
    }

    /**
     * Exibe a página para registrar a produção diária.
     * @param model O modelo para a view.
     * @param data Data da produção (opcional).
     * @return O template "producao".
     */
    @GetMapping("/producao/registrar")
    public String registrarProducao(Model model, @RequestParam(value = "data", required = false) String data) {
        List<Produto> produtos = produtoService.findAll();
        model.addAttribute("produtos", produtos);
        if (data != null) {
            model.addAttribute("dataProducao", data);
        }
        else {
            model.addAttribute("dataProducao", LocalDate.now().toString());
        }
        return "producao";
    }

    /**
     * Redireciona a rota base de produção para a visão de produção diária.
     * @return Redirecionamento para "/producao/diaria".
     */
    @GetMapping("/producao")
    public String producaoIndex() {
        return "redirect:/producao/diaria";
    }

    /**
     * Processa o registro de uma nova produção, validando e consumindo o estoque de ingredientes.
     * @param producaoRequest Objeto com os dados da produção.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a tela de produção diária.
     */
    @PostMapping("/producao/diaria")
    public String registrarProducao(@ModelAttribute("producaoRequest") ProducaoRequest producaoRequest, RedirectAttributes redirectAttributes) {
        log.info("ProducaoRequest: {}", producaoRequest);
        if (producaoRequest.getProdutos() != null) {
            for (ProducaoDTO dto : producaoRequest.getProdutos()) {
                log.info("ProducaoDTO: produtoId={}, quantidade={}", dto.getProdutoId(), dto.getQuantidade());
            }
        }
        log.info("Registrando produção para {} produtos", producaoRequest.getProdutos() != null ? producaoRequest.getProdutos().size() : 0);
        LocalDate dataProducao = LocalDate.parse(producaoRequest.getDataProducao());

        if (producaoRequest.getProdutos() == null || producaoRequest.getProdutos().isEmpty()) {
            redirectAttributes.addFlashAttribute("errosDeEstoque", "Nenhum produto foi adicionado à produção.");
            return "redirect:/producao/registrar?data=" + producaoRequest.getDataProducao();
        }

        List<String> errosDeEstoque = producaoService.validarEstoque(producaoRequest.getProdutos());
        if (!errosDeEstoque.isEmpty()) {
            redirectAttributes.addFlashAttribute("errosDeEstoque", errosDeEstoque);
            return "redirect:/producao/registrar?data=" + producaoRequest.getDataProducao();
        }

        producaoService.registrarProducao(producaoRequest.getProdutos(), dataProducao);
        return "redirect:/producao/diaria?data=" + dataProducao.toString();
    }

    /**
     * Exibe os registros de produção de uma data específica.
     * @param dataStr A data da produção. Se não for fornecida, usa a data atual.
     * @param model O modelo para a view.
     * @return O template "producaoDiaria".
     */
    @GetMapping("/producao/diaria")
    public String producaoDiaria(@RequestParam(value = "data", required = false) String dataStr, Model model) {
        LocalDate data;
        if (dataStr != null && !dataStr.isEmpty()) {
            data = LocalDate.parse(dataStr);
        }
        else {
            data = LocalDate.now();
        }
        List<Producao> producoes = producaoService.findByDataProducao(data);
        model.addAttribute("producoes", producoes);
        model.addAttribute("data", data);
        return "producaoDiaria";
    }

    /**
     * Aumenta em 1 a quantidade de um lote de produção.
     * @param id O ID da produção.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a tela de produção diária.
     */
    @PostMapping("/producao/diaria/aumentar/{id}")
    public String aumentarProducao(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        producaoService.aumentarProducao(id);
        return "redirect:/producao/diaria?data=" + producaoService.findById(id).map(p -> p.getDataProducao().toString()).orElse("");
    }

    /**
     * Diminui em 1 a quantidade de um lote de produção.
     * @param id O ID da produção.
     * @return Redirecionamento para a tela de produção diária.
     */
    @PostMapping("/producao/diaria/diminuir/{id}")
    public String diminuirProducao(@PathVariable("id") Long id) {
        producaoService.diminuirProducao(id);
        return "redirect:/producao/diaria?data=" + producaoService.findById(id).map(p -> p.getDataProducao().toString()).orElse("");
    }

    /**
     * Remove um registro de produção e suas vendas associadas.
     * @param id O ID da produção.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a tela de produção diária.
     */
    @PostMapping("/producao/diaria/remover/{id}")
    @Transactional
    public String removerProducao(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        producaoService.removerProducao(id);
        redirectAttributes.addFlashAttribute("success", "Produção e vendas associadas removidas com sucesso.");
        return "redirect:/producao/diaria?data=" + producaoService.findById(id).map(p -> p.getDataProducao().toString()).orElse("");
    }

    /**
     * Exibe a página de configurações do usuário.
     * @param model O modelo para a view.
     * @return O template "configuracoes".
     */
    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "configuracoes";
    }

    /**
     * Processa a redefinição de senha do usuário logado.
     * @param senhaAntiga A senha atual do usuário.
     * @param novaSenha A nova senha.
     * @param confirmarNovaSenha A confirmação da nova senha.
     * @param principal O usuário autenticado.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de configurações.
     */
    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam String senhaAntiga,
                                 @RequestParam String novaSenha,
                                 @RequestParam String confirmarNovaSenha,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirSenha(principal.getName(), senhaAntiga, novaSenha, confirmarNovaSenha);
            redirectAttributes.addFlashAttribute("success", "Senha redefinida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracoes";
    }

    /**
     * Processa a redefinição de login (username) do usuário.
     * @param loginAtual O login atual do usuário.
     * @param novoLogin O novo login desejado.
     * @param senha A senha do usuário para confirmação.
     * @param principal O usuário autenticado.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de login ou configurações.
     */
    @PostMapping("/redefinir-login")
    public String redefinirLogin(@RequestParam String loginAtual,
                                 @RequestParam String novoLogin,
                                 @RequestParam String senha,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirLogin(principal.getName(), loginAtual, novoLogin, senha);
            redirectAttributes.addFlashAttribute("successLogin", "Nome de usuário redefinido com sucesso. Por favor, faça login novamente.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorLogin", e.getMessage());
            return "redirect:/configuracoes";
        }
    }

    /**
     * Exibe a página inicial do fluxo "Esqueci minha senha".
     * @return O template "esqueciSenha".
     */
    @GetMapping("/esqueci-senha")
    public String esqueciSenha() {
        return "esqueciSenha";
    }

    /**
     * Exibe o formulário de cadastro de um novo usuário.
     * @param model O modelo para a view.
     * @return O template "cadastroUsuario".
     */
    @GetMapping("/cadastroUsuario")
    public String cadastroUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastroUsuario";
    }

    /**
     * Processa o cadastro de um novo usuário.
     * @param usuario O usuário a ser salvo.
     * @param confirmarSenha A confirmação da senha.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de login ou de cadastro.
     */
    @PostMapping("/salvarUsuario")
    public String salvarUsuario(@ModelAttribute("usuario") Usuario usuario, @RequestParam("confirmarSenha") String confirmarSenha, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.salvarUsuario(usuario, confirmarSenha);
            redirectAttributes.addFlashAttribute("success", "Usuário cadastrado com sucesso! Aguarde a aprovação do administrador.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cadastroUsuario";
        }
    }

    /**
     * Processa o primeiro passo do fluxo "Esqueci minha senha", validando o login.
     * @param login O login do usuário.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página da pergunta secreta ou de volta em caso de erro.
     */
    @PostMapping("/esqueci-senha")
    public String processEsqueciSenha(@RequestParam String login, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.processEsqueciSenha(login);
            redirectAttributes.addAttribute("login", login);
            return "redirect:/redefinir-senha-pergunta";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/esqueci-senha";
        }
    }

    /**
     * Exibe a página com a pergunta secreta do usuário.
     * @param login O login do usuário.
     * @param model O modelo para a view.
     * @return O template "redefinirSenhaPergunta".
     */
    @GetMapping("/redefinir-senha-pergunta")
    public String redefinirSenhaPergunta(@RequestParam String login, Model model) {
        usuarioService.findByLogin(login).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "redefinirSenhaPergunta";
    }

    /**
     * Verifica se a resposta secreta fornecida pelo usuário está correta.
     * @param login O login do usuário.
     * @param respostaSecreta A resposta fornecida.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de nova senha ou de volta em caso de erro.
     */
    @PostMapping("/verificar-resposta")
    public String verificarResposta(@RequestParam String login, @RequestParam String respostaSecreta, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.verificarResposta(login, respostaSecreta);
            redirectAttributes.addAttribute("login", login);
            return "redirect:/nova-senha";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("login", login);
            return "redirect:/redefinir-senha-pergunta";
        }
    }

    /**
     * Exibe a página para o usuário digitar a nova senha.
     * @param login O login do usuário.
     * @param model O modelo para a view.
     * @return O template "novaSenha".
     */
    @GetMapping("/nova-senha")
    public String novaSenha(@RequestParam String login, Model model) {
        model.addAttribute("login", login);
        return "novaSenha";
    }

    /**
     * Salva a nova senha definida pelo usuário no fluxo "Esqueci minha senha".
     * @param login O login do usuário.
     * @param novaSenha A nova senha.
     * @param confirmarNovaSenha A confirmação da nova senha.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de login ou de volta em caso de erro.
     */
    @PostMapping("/salvar-nova-senha")
    public String salvarNovaSenha(@RequestParam String login, @RequestParam String novaSenha, @RequestParam String confirmarNovaSenha, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.salvarNovaSenha(login, novaSenha, confirmarNovaSenha);
            redirectAttributes.addFlashAttribute("success", "Senha redefinida com sucesso.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("login", login);
            return "redirect:/nova-senha";
        }
    }

    /**
     * Processa a redefinição da pergunta e resposta secretas do usuário.
     * @param senha A senha atual para confirmação.
     * @param novaPerguntaSecreta A nova pergunta.
     * @param novaRespostaSecreta A nova resposta.
     * @param principal O usuário autenticado.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a página de configurações.
     */
    @PostMapping("/redefinir-pergunta")
    public String redefinirPergunta(@RequestParam String senha, @RequestParam String novaPerguntaSecreta, @RequestParam String novaRespostaSecreta, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirPergunta(principal.getName(), senha, novaPerguntaSecreta, novaRespostaSecreta);
            redirectAttributes.addFlashAttribute("successPergunta", "Pergunta secreta redefinida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorPergunta", e.getMessage());
        }
        return "redirect:/configuracoes";
    }

    /**
     * Endpoint da API para obter eventos de venda para o calendário.
     * @return Lista de {@link VendaEventoDTO} em formato JSON.
     */
    @GetMapping("/api/vendas/eventos")
    @ResponseBody
    public List<VendaEventoDTO> getVendaEventos() {
        return vendaService.getVendaEventos();
    }

    /**
     * Endpoint da API para obter detalhes de vendas de uma data específica.
     * @param data A data da busca.
     * @return Lista de {@link VendaDetalheDTO} em formato JSON.
     */
    @GetMapping("/api/vendas/data")
    @ResponseBody
    public List<VendaDetalheDTO> getVendasPorData(@RequestParam("data") String data) {
        return vendaService.getVendasPorData(data);
    }

    /**
     * Exibe a página com o calendário de produção.
     * @return O template "calendarioProducao".
     */
    @GetMapping("/calendario-producao")
    public String calendarioProducao() {
        return "calendarioProducao";
    }

    /**
     * Endpoint da API para obter eventos de produção para o calendário.
     * @return Lista de {@link ProducaoEventoDTO} em formato JSON.
     */
    @GetMapping("/api/producao/eventos")
    @ResponseBody
    public List<ProducaoEventoDTO> getProducaoEventos() {
        return producaoService.getProducaoEventos();
    }

    /**
     * Endpoint da API para obter os registros de produção de uma data específica.
     * @param data A data da busca.
     * @return Lista de {@link Producao} em formato JSON.
     */
    @GetMapping("/api/producao/data")
    @ResponseBody
    public List<Producao> getProducaoPorData(@RequestParam("data") String data) {
        return producaoService.getProducaoPorData(data);
    }

    /**
     * Exibe os detalhes das vendas de um dia específico.
     * @param dataStr A data da busca.
     * @param model O modelo para a view.
     * @return O template "vendasDia".
     */
    @GetMapping("/vendas/dia")
    public String vendasDia(@RequestParam("data") String dataStr, Model model) {
        LocalDate data = LocalDate.parse(dataStr);
        List<Venda> vendas = vendaService.findByDataVendaWithProducaoAndProduto(data);
        model.addAttribute("vendas", vendas);
        model.addAttribute("data", data);
        return "vendasDia";
    }

    /**
     * Exibe o formulário para registrar uma nova venda.
     * @param dataStr A data da venda (opcional).
     * @param model O modelo para a view.
     * @return O template "novaVenda".
     */
    @GetMapping("/venda/nova")
    public String novaVenda(@RequestParam(value = "data", required = false) String dataStr, Model model) {
        LocalDate data;
        if (dataStr == null || dataStr.isEmpty()) {
            data = LocalDate.now();
        }
        else {
            data = LocalDate.parse(dataStr);
        }
        
        List<ProducaoDisponivelDTO> producoesDisponiveis = producaoService.findProducoesComEstoqueDisponivel();
        
        model.addAttribute("producoes", producoesDisponiveis);
        model.addAttribute("data", data);
        return "novaVenda";
    }
    

    /**
     * Salva uma nova venda.
     * @param producaoId O ID do lote de produção vendido.
     * @param quantidade A quantidade vendida.
     * @param valorVenda O valor unitário da venda.
     * @param formaPagamento A forma de pagamento.
     * @param doado Flag indicando se foi uma doação.
     * @param dataVenda A data da venda.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de vendas.
     */
    @PostMapping("/venda/nova")
    @Transactional
    public String salvarVenda(@RequestParam Long producaoId,
                              @RequestParam int quantidade,
                              @RequestParam double valorVenda,
                              @RequestParam String formaPagamento,
                              @RequestParam(required = false) boolean doado,
                              @RequestParam String dataVenda,
                              RedirectAttributes redirectAttributes) {

        VendaRequest vendaRequest = new VendaRequest();
        vendaRequest.setDataVenda(dataVenda);
        vendaRequest.setFormaPagamento(formaPagamento);
        vendaRequest.setDoado(doado);

        ItemVendaDTO item = new ItemVendaDTO();
        item.setProducaoId(producaoId);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorVenda);

        vendaRequest.setItens(List.of(item));

        try {
            vendaService.salvarVenda(vendaRequest);
            redirectAttributes.addFlashAttribute("success", "Venda registrada com sucesso!");
            return "redirect:/vendas";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/venda/nova?data=" + dataVenda;
        }
    }

    /**
     * Remove uma venda do sistema.
     * @param id O ID da venda a ser removida.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de vendas.
     */
    @PostMapping("/venda/remover/{id}")
    public String removerVenda(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vendaService.removerVenda(id);
            redirectAttributes.addFlashAttribute("success", "Venda removida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendas";
    }

    /**
     * Exibe o formulário para editar uma venda existente.
     * @param id O ID da venda a ser editada.
     * @param model O modelo para a view.
     * @param redirectAttributes Atributos para mensagens de feedback em caso de erro.
     * @return O template "formularioEditaVenda".
     */
    @GetMapping("/venda/editar/{id}")
    public String editarVenda(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            VendaRequest vendaRequest = vendaService.findVendaRequestById(id);
            model.addAttribute("vendaRequest", vendaRequest);
            model.addAttribute("vendaId", id);
            // A lista de produções é necessária para exibir o nome do produto no formulário.
            List<ProducaoDisponivelDTO> producoesDisponiveis = producaoService.findProducoesComEstoqueDisponivel();
            model.addAttribute("producoes", producoesDisponiveis);
            return "formularioEditaVenda";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendas";
        }
    }

    /**
     * Processa a atualização de uma venda existente.
     * @param id O ID da venda a ser atualizada.
     * @param producaoId O ID do lote de produção (não pode ser alterado na edição).
     * @param quantidade A nova quantidade vendida.
     * @param valorVenda O novo valor unitário da venda.
     * @param formaPagamento A nova forma de pagamento.
     * @param doado Flag indicando se foi uma doação.
     * @param dataVenda A nova data da venda.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de vendas.
     */
    @PostMapping("/venda/editar/{id}")
    public String atualizarVenda(@PathVariable("id") Long id,
                                 @RequestParam Long producaoId,
                                 @RequestParam int quantidade,
                                 @RequestParam double valorVenda,
                                 @RequestParam String formaPagamento,
                                 @RequestParam(required = false) boolean doado,
                                 @RequestParam String dataVenda,
                                 RedirectAttributes redirectAttributes) {

        VendaRequest vendaRequest = new VendaRequest();
        vendaRequest.setDataVenda(dataVenda);
        vendaRequest.setFormaPagamento(formaPagamento);
        vendaRequest.setDoado(doado);

        ItemVendaDTO item = new ItemVendaDTO();
        item.setProducaoId(producaoId);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorVenda);

        vendaRequest.setItens(List.of(item));

        try {
            vendaService.atualizarVenda(id, vendaRequest);
            redirectAttributes.addFlashAttribute("success", "Venda atualizada com sucesso!");
            return "redirect:/vendas";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/venda/editar/" + id;
        }
    }

    /**
     * Exibe o relatório de vendas filtrado por período.
     * @param dataInicioStr Data de início do período.
     * @param dataFimStr Data de fim do período.
     * @param model O modelo para a view.
     * @return O template "relatorioVendas".
     */
    @GetMapping("/relatorio/vendas")
    public String relatorioVendas(@RequestParam(value = "dataInicio", required = false) String dataInicioStr,
                                  @RequestParam(value = "dataFim", required = false) String dataFimStr,
                                  Model model) {
        List<Venda> vendas = vendaService.findVendasByPeriod(dataInicioStr, dataFimStr);
        int totalQuantidade = vendas.stream().flatMap(venda -> venda.getItens().stream()).mapToInt(ItemVenda::getQuantidade).sum();
        double totalValor = vendas.stream().mapToDouble(Venda::getValorVenda).sum();

        model.addAttribute("vendas", vendas);
        model.addAttribute("dataInicio", dataInicioStr != null ? LocalDate.parse(dataInicioStr) : null);
        model.addAttribute("dataFim", dataFimStr != null ? LocalDate.parse(dataFimStr) : null);
        model.addAttribute("totalQuantidade", totalQuantidade);
        model.addAttribute("totalValor", totalValor);

        return "relatorioVendas";
    }

    /**
     * Remove uma quantidade específica de um lote de produção (ex: por perda ou descarte).
     * @param producaoId O ID do lote de produção.
     * @param quantidadeARemover A quantidade a ser removida.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para o calendário de produção.
     */
    @PostMapping("/producao/diaria/remover-quantidade")
    public String removerQuantidadeProducao(@RequestParam Long producaoId, 
                                            @RequestParam int quantidadeARemover, 
                                            RedirectAttributes redirectAttributes) {
        try {
            producaoService.removerQuantidadeProducao(producaoId, quantidadeARemover);
            redirectAttributes.addFlashAttribute("modalSuccess", quantidadeARemover + " iten(s) removido(s) da produção com sucesso. Estoque atualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("modalError", e.getMessage());
        }
        return "redirect:/calendario-producao";
    }

    /**
     * Endpoint de administrador para recalcular o preço de todos os produtos.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de produtos.
     */
    @GetMapping("/admin/recalcular-precos")
    public String recalcularPrecos(RedirectAttributes redirectAttributes) {
        int produtosAtualizados = produtoService.recalcularPrecos();
        redirectAttributes.addFlashAttribute("success", produtosAtualizados + " produto(s) tiveram seus preços recalculados e atualizados com sucesso!");
        return "redirect:/produtos";
    }

    // Pedidos

    /**
     * Exibe a lista de pedidos, com filtros opcionais.
     * @param cliente Filtro por nome do cliente.
     * @param status Filtro por status do pedido.
     * @param dataStr Filtro por data de entrega.
     * @param model O modelo para a view.
     * @return O template "listaPedidos".
     */
    @GetMapping("/pedidos")
    public String listarPedidos(@RequestParam(name = "cliente", required = false) String cliente,
                              @RequestParam(name = "status", required = false) String status,
                              @RequestParam(name = "data", required = false) String dataStr,
                              Model model) {
        List<Pedido> pedidos = pedidoService.findWithFilters(cliente, status, dataStr);
        model.addAttribute("pedidos", pedidos);
        return "listaPedidos";
    }

    /**
     * Exibe o formulário para criar um novo pedido.
     * @param model O modelo para a view.
     * @return O template "formularioPedido".
     */
    @GetMapping("/pedido/novo")
    public String novoPedido(Model model) {
        model.addAttribute("pedido", new PedidoRequest());
        model.addAttribute("produtos", produtoService.findAll());
        return "formularioPedido";
    }

    /**
     * Salva um novo pedido.
     * @param pedidoRequest Objeto com os dados do pedido.
     * @param result Resultado da validação.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de pedidos.
     */
    @PostMapping("/pedido/salvar")
    public String salvarPedido(@Valid PedidoRequest pedidoRequest, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/pedido/novo";
        }
        try {
            pedidoService.salvarPedido(pedidoRequest);
            redirectAttributes.addFlashAttribute("success", "Pedido salvo com sucesso!");
            return "redirect:/pedidos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedido/novo";
        }
    }

    /**
     * Exibe a página de detalhes de um pedido.
     * @param id O ID do pedido.
     * @param model O modelo para a view.
     * @return O template "detalhesPedido".
     */
    @GetMapping("/pedido/{id}")
    public String detalhesPedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.findByIdWithItens(id);
        model.addAttribute("pedido", pedido);
        return "detalhesPedido";
    }

    /**
     * Exclui um pedido.
     * @param id O ID do pedido a ser excluído.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de pedidos.
     */
    @PostMapping("/pedido/excluir/{id}")
    public String excluirPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.excluirPedido(id);
            redirectAttributes.addFlashAttribute("success", "Pedido excluído com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos";
    }

    /**
     * Atualiza o status de um pedido (ex: de 'Pendente' para 'Concluído').
     * @param id O ID do pedido.
     * @param status O novo status.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de pedidos.
     */
    @PostMapping("/pedido/atualizar-status/{id}")
    public String atualizarStatusPedido(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Status do pedido atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos";
    }

    /**
     * Exibe a lista de usuários para administração.
     * @param model O modelo para a view.
     * @return O template "listaUsuarios".
     */
    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaUsuarios";
    }

    /**
     * Aprova o cadastro de um novo usuário.
     * @param id O ID do usuário a ser aprovado.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de usuários.
     */
    @PostMapping("/admin/usuarios/aprovar/{id}")
    public String aprovarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.aprovarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuário aprovado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Reprova (desativa) o cadastro de um usuário.
     * @param id O ID do usuário a ser reprovado.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de usuários.
     */
    @PostMapping("/admin/usuarios/reprovar/{id}")
    public String reprovarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.reprovarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuário reprovado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Exclui um usuário do sistema.
     * @param id O ID do usuário a ser excluído.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de usuários.
     */
    @PostMapping("/admin/usuarios/excluir/{id}")
    public String excluirUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.excluirUsuario(id);
        redirectAttributes.addFlashAttribute("success", "Usuário excluído com sucesso!");
        return "redirect:/admin/usuarios";
    }

    // Orçamentos

    /**
     * Exibe a lista de todos os orçamentos.
     * @param model O modelo para a view.
     * @return O template "listaOrcamentos".
     */
    @GetMapping("/orcamentos")
    public String listarOrcamentos(Model model) {
        model.addAttribute("orcamentos", orcamentoService.findAll());
        return "listaOrcamentos";
    }

    /**
     * Exibe o formulário para criar um novo orçamento.
     * @param model O modelo para a view.
     * @return O template "formularioOrcamento".
     */
    @GetMapping("/orcamento/novo")
    public String novoOrcamento(Model model) {
        model.addAttribute("orcamento", new OrcamentoRequest());
        model.addAttribute("produtos", produtoService.findAll());
        return "formularioOrcamento";
    }

    /**
     * Salva um novo orçamento ou atualiza um existente.
     * @param orcamentoRequest Objeto com os dados do orçamento.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de orçamentos.
     */
    @PostMapping("/orcamento/salvar")
    @Transactional
    public String salvarOrcamento(@Valid OrcamentoRequest orcamentoRequest, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/orcamento/novo";
        }
        try {
            orcamentoService.salvarOrcamento(orcamentoRequest);
            redirectAttributes.addFlashAttribute("success", "Orçamento salvo com sucesso!");
            return "redirect:/orcamentos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orcamento/novo";
        }
    }

    /**
     * Exclui um orçamento.
     * @param id O ID do orçamento a ser excluído.
     * @param redirectAttributes Atributos para mensagens de feedback.
     * @return Redirecionamento para a lista de orçamentos.
     */
    @PostMapping("/orcamento/excluir/{id}")
    public String excluirOrcamento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orcamentoService.excluirOrcamento(id);
            redirectAttributes.addFlashAttribute("success", "Orçamento excluído com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orcamentos";
    }

    /**
     * Exibe o formulário de orçamento preenchido para edição.
     * @param id O ID do orçamento a ser editado.
     * @param model O modelo para a view.
     * @return O template "formularioOrcamento".
     */
    @GetMapping("/orcamento/editar/{id}")
    public String editarOrcamento(@PathVariable Long id, Model model) {
        try {
            OrcamentoRequest orcamentoRequest = orcamentoService.findOrcamentoRequestById(id);
            model.addAttribute("orcamento", orcamentoRequest);
            model.addAttribute("produtos", produtoService.findAll());
            return "formularioOrcamento";
        } catch (IllegalArgumentException e) {
            return "redirect:/orcamentos";
        }
    }
    
    /**
     * Endpoint da API para obter sugestões de matérias-primas (autocomplete).
     * @param termo O termo de busca.
     * @return Lista de {@link MateriaPrima} em formato JSON.
     */
    @GetMapping("/api/materia-prima/sugestoes")
    @ResponseBody
    public List<MateriaPrima> sugerirMateriasPrimas(@RequestParam("termo") String termo) {
        return materiaPrimaRepository.findByNomeContainingIgnoreCase(termo);
    }

    /**
     * Endpoint da API para obter sugestões de produtos (autocomplete).
     * @param termo O termo de busca.
     * @return Lista de {@link Produto} em formato JSON.
     */
    @GetMapping("/api/produtos/sugestoes")
    @ResponseBody
    public List<Produto> sugerirProdutos(@RequestParam("termo") String termo) {
        return produtoRepository.findByNomeContainingIgnoreCase(termo);
    }
}
