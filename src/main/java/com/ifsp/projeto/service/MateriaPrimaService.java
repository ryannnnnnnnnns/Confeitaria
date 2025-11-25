package com.ifsp.projeto.service;

import com.ifsp.projeto.controller.dto.MateriaPrimaComAlertaDTO;
import com.ifsp.projeto.model.MateriaPrima;
import com.ifsp.projeto.repository.IngredienteRepository;
import com.ifsp.projeto.repository.MateriaPrimaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar a lógica de negócio de Matérias-Primas (estoque).
 * Responsável por cadastrar, atualizar, excluir e consultar matérias-primas.
 * Inclui lógica para conversão de unidades, cálculo de custo médio ponderado e verificação de estoque baixo.
 */
@Service
public class MateriaPrimaService {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final IngredienteRepository ingredienteRepository;

    public MateriaPrimaService(MateriaPrimaRepository materiaPrimaRepository, IngredienteRepository ingredienteRepository) {
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    /**
     * Busca todas as matérias-primas cadastradas.
     * @return Uma lista de todas as matérias-primas.
     */
    public List<MateriaPrima> findAll() {
        return materiaPrimaRepository.findAll();
    }

    /**
     * Busca uma matéria-prima pelo seu ID.
     * @param id O ID da matéria-prima.
     * @return Um {@link Optional} contendo a matéria-prima, ou vazio se não encontrada.
     */
    public Optional<MateriaPrima> findById(Long id) {
        return materiaPrimaRepository.findById(id);
    }

    /**
     * Salva uma nova matéria-prima ou atualiza os dados de uma existente (exceto quantidade).
     * Converte unidades (kg->g, L->ml) e calcula o valor unitário inicial. Impede o cadastro de duplicatas (mesmo nome e unidade).
     * @param materiaPrima A entidade {@link MateriaPrima} a ser salva.
     * @return A matéria-prima salva.
     * @throws IllegalStateException Se já existir uma matéria-prima com o mesmo nome e unidade.
     */
    @Transactional
    public MateriaPrima salvarMP(MateriaPrima materiaPrima) {
        if (materiaPrima.getUnidade() != null) {
            String unidade = materiaPrima.getUnidade().toLowerCase();
            if (unidade.equals("kg")) {
                materiaPrima.setQuantidade(materiaPrima.getQuantidade() * 1000);
                if (materiaPrima.getQuantidadeMinima() != null) {
                    materiaPrima.setQuantidadeMinima(materiaPrima.getQuantidadeMinima() * 1000);
                }
                materiaPrima.setUnidade("g");
            } else if (unidade.equals("l")) {
                materiaPrima.setQuantidade(materiaPrima.getQuantidade() * 1000);
                if (materiaPrima.getQuantidadeMinima() != null) {
                    materiaPrima.setQuantidadeMinima(materiaPrima.getQuantidadeMinima() * 1000);
                }
                materiaPrima.setUnidade("ml");
            }
        }

        Optional<MateriaPrima> materiaPrimaExistente = materiaPrimaRepository.findByNomeAndUnidade(materiaPrima.getNome(), materiaPrima.getUnidade());
        if (materiaPrimaExistente.isPresent() && (materiaPrima.getId() == null || !materiaPrima.getId().equals(materiaPrimaExistente.get().getId()))) {
            throw new IllegalStateException("Matéria-prima já cadastrada com este nome e unidade.");
        }

        if (materiaPrima.getQuantidade() != null && materiaPrima.getQuantidade() > 0 && materiaPrima.getValor() != null) {
            materiaPrima.setValor(materiaPrima.getValor() / materiaPrima.getQuantidade());
        }
        return materiaPrimaRepository.save(materiaPrima);
    }

    /**
     * Exclui uma matéria-prima do estoque. A exclusão só é permitida se a matéria-prima não estiver sendo utilizada como ingrediente em nenhum produto.
     * @param id O ID da matéria-prima a ser excluída.
     * @throws IllegalStateException Se a matéria-prima estiver em uso e não puder ser excluída.
     */
    @Transactional
    public void excluirMP(Long id) {
        if (ingredienteRepository.existsByMateriaPrimaId(id)) {
            throw new IllegalStateException("Não é possível excluir a matéria-prima, pois ela está sendo utilizada em um ou mais produtos.");
        }
        materiaPrimaRepository.deleteById(id);
    }

    /**
     * Adiciona uma nova quantidade de uma matéria-prima já existente ao estoque e recalcula o valor unitário com base no custo médio ponderado.
     * @param materiaPrima Uma entidade {@link MateriaPrima} contendo os dados da entrada (nome, unidade, quantidade e valor da compra).
     * @return A matéria-prima atualizada com a nova quantidade e o novo valor unitário.
     * @throws IllegalArgumentException Se a matéria-prima não estiver previamente cadastrada.
     */
    @Transactional
    public MateriaPrima adicionarMP(MateriaPrima materiaPrima) {
        if (materiaPrima.getUnidade() != null) {
            String unidade = materiaPrima.getUnidade().toLowerCase();
            if (unidade.equals("kg")) {
                materiaPrima.setQuantidade(materiaPrima.getQuantidade() * 1000);
                materiaPrima.setUnidade("g");
            } else if (unidade.equals("l")) {
                materiaPrima.setQuantidade(materiaPrima.getQuantidade() * 1000);
                materiaPrima.setUnidade("ml");
            }
        }

        MateriaPrima materiaPrimaExistente = materiaPrimaRepository
                .findByNomeAndUnidade(materiaPrima.getNome(), materiaPrima.getUnidade())
                .orElseThrow(() -> new IllegalArgumentException("Matéria-prima não cadastrada. Cadastre-a primeiro."));

        double quantidadeAdicionada = materiaPrima.getQuantidade();
        double valorAdicionado = materiaPrima.getValor() != null ? materiaPrima.getValor() : 0;

        double quantidadeAntiga = materiaPrimaExistente.getQuantidade();
        double valorUnitarioAntigo = materiaPrimaExistente.getValor() != null ? materiaPrimaExistente.getValor() : 0;

        double valorTotalAntigo = quantidadeAntiga * valorUnitarioAntigo;

        double quantidadeNovaTotal = quantidadeAntiga + quantidadeAdicionada;
        double valorTotalNovo = valorTotalAntigo + valorAdicionado;

        materiaPrimaExistente.setQuantidade(quantidadeNovaTotal);
        if (quantidadeNovaTotal > 0) {
            materiaPrimaExistente.setValor(valorTotalNovo / quantidadeNovaTotal);
        } else {
            materiaPrimaExistente.setValor(valorUnitarioAntigo);
        }

        return materiaPrimaRepository.save(materiaPrimaExistente);
    }

    /**
     * Encontra todas as matérias-primas que estão com estoque baixo, comparando a quantidade atual com a quantidade mínima definida.
     * @return Uma lista de matérias-primas com estoque baixo.
     */
    public List<MateriaPrima> findLowStock() {
        return materiaPrimaRepository.findAll().stream()
                .filter(MateriaPrima::isLowStock)
                .collect(Collectors.toList());
    }

    /**
     * Busca todas as matérias-primas aplicando filtros e ordenação, e indica se o estoque está baixo.
     * @param nome Filtro opcional para o nome da matéria-prima.
     * @param unidade Filtro opcional para a unidade de medida.
     * @param sort Campo opcional para ordenação ('quantidade' ou 'valor').
     * @param order Ordem opcional ('asc' ou 'desc').
     * @return Lista de {@link MateriaPrimaComAlertaDTO} com os dados e o alerta de estoque.
     */
    public List<MateriaPrimaComAlertaDTO> findAllWithAlert(String nome, String unidade, String sort, String order) {
        List<MateriaPrima> materiasPrimas = materiaPrimaRepository.findAll();

        if (nome != null && !nome.isEmpty()) {
            materiasPrimas = materiasPrimas.stream()
                .filter(mp -> mp.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
        }

        if (unidade != null && !unidade.isEmpty()) {
            materiasPrimas = materiasPrimas.stream()
                .filter(mp -> mp.getUnidade().equals(unidade))
                .collect(Collectors.toList());
        }

        if (sort != null && !sort.isEmpty()) {
            Comparator<MateriaPrima> comparator = null;
            if (sort.equals("quantidade")) {
                comparator = Comparator.comparing(MateriaPrima::getQuantidade);
            } else if (sort.equals("valor")) {
                comparator = Comparator.comparing(MateriaPrima::getValor);
            }

            if (comparator != null) {
                if (order != null && order.equals("desc")) {
                    comparator = comparator.reversed();
                }
                materiasPrimas = materiasPrimas.stream().sorted(comparator).collect(Collectors.toList());
            }
        }

        return materiasPrimas.stream()
                .map(mp -> new MateriaPrimaComAlertaDTO(mp, mp.isLowStock()))
                .collect(Collectors.toList());
    }
}
