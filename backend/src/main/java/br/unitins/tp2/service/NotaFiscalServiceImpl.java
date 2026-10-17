package br.unitins.tp2.service;

import java.util.ArrayList;

import br.unitins.tp2.dto.NotaFiscalRequest;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ResourceConflictException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.ItemNotaFiscal;
import br.unitins.tp2.model.NotaFiscal;
import br.unitins.tp2.model.Marca;
import br.unitins.tp2.model.StatusVeiculo;
import br.unitins.tp2.model.Veiculo;
import br.unitins.tp2.repository.ClienteRepository;
import br.unitins.tp2.repository.FornecedorRepository;
import br.unitins.tp2.repository.NotaFiscalRepository;
import br.unitins.tp2.repository.ProdutoRepository;
import br.unitins.tp2.repository.VeiculoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NotaFiscalServiceImpl implements NotaFiscalService {
    @Inject NotaFiscalRepository notas;
    @Inject ClienteRepository clientes;
    @Inject FornecedorRepository fornecedores;
    @Inject ProdutoRepository produtos;
    @Inject VeiculoRepository veiculos;

    @Override
    @Transactional
    public NotaFiscal criar(NotaFiscalRequest request) {
        if (request.entrada() ? request.fornecedorId() == null || request.clienteId() != null
                : request.clienteId() == null || request.fornecedorId() != null) {
            throw ValidationException.of("clienteId",
                    "Notas de entrada exigem fornecedorId e notas de saída exigem clienteId; informe apenas o vínculo correspondente.");
        }
        var nota = new NotaFiscal();
        nota.setNumero(request.numero());
        nota.setSerie(request.serie());
        nota.setDataEmissao(request.dataEmissao());
        nota.setEntrada(request.entrada());
        if (request.entrada()) {
            var fornecedor = fornecedores.findById(request.fornecedorId());
            if (fornecedor == null) throw new ResourceNotFoundException("Fornecedor não encontrado.");
            nota.setFornecedor(fornecedor);
        } else {
            var cliente = clientes.findById(request.clienteId());
            if (cliente == null) throw new ResourceNotFoundException("Cliente não encontrado.");
            nota.setCliente(cliente);
        }
        var itens = new ArrayList<ItemNotaFiscal>();
        for (var input : request.itens()) {
            if ((input.produtoId() == null) == (input.veiculoNovo() == null)) {
                throw ValidationException.of("itens", "Cada item deve informar produtoId ou veiculoNovo, exclusivamente.");
            }
            if (input.veiculoNovo() != null && !request.entrada()) {
                throw ValidationException.of("itens.veiculoNovo", "Veículos novos só podem ser incluídos em nota de entrada.");
            }
            if (input.veiculoNovo() != null && input.quantidade() != 1) {
                throw ValidationException.of("itens.quantidade", "Cada veículo individual deve ter quantidade igual a 1.");
            }
            var produto = input.veiculoNovo() == null ? produtos.findById(input.produtoId()) : criarVeiculo(input.veiculoNovo());
            if (produto == null) throw new ResourceNotFoundException("Produto não encontrado: " + input.produtoId());
            if (request.entrada() && input.veiculoNovo() == null && produto instanceof Veiculo) {
                throw ValidationException.of("itens.veiculoNovo", "Informe os dados do veículo para cadastrá-lo na nota de entrada.");
            }
            if (!request.entrada() && produto instanceof Veiculo veiculo) {
                if (veiculo.getStatus() == StatusVeiculo.VENDIDO) {
                    throw new ResourceConflictException("O veículo já consta como vendido.");
                }
                veiculo.setStatus(StatusVeiculo.VENDIDO);
            }
            var item = new ItemNotaFiscal();
            item.setProduto(produto);
            item.setQuantidade(input.quantidade());
            item.setValorUnitario(input.valorUnitario());
            itens.add(item);
        }
        nota.substituirItens(itens);
        notas.persistAndFlush(nota);
        return nota;
    }

    private Veiculo criarVeiculo(NotaFiscalRequest.VeiculoNovo input) {
        Marca marca = veiculos.getEntityManager().find(Marca.class, input.marcaId());
        if (marca == null) throw new ResourceNotFoundException("Marca não encontrada.");
        var veiculo = new Veiculo();
        veiculo.setDescricao(input.descricao());
        veiculo.setValorCusto(input.valorCusto());
        veiculo.setValorVenda(input.valorVenda());
        veiculo.setAtivo(true);
        veiculo.setModelo(input.modelo());
        veiculo.setAnoFabricacao(input.anoFabricacao());
        veiculo.setAnoModelo(input.anoModelo());
        veiculo.setCor(input.cor());
        veiculo.setChassi(input.chassi());
        veiculo.setPlaca(input.placa());
        veiculo.setRenavam(input.renavam());
        veiculo.setQuilometragem(input.quilometragem());
        veiculo.setMarca(marca);
        veiculo.setStatus(StatusVeiculo.DISPONIVEL);
        veiculos.persistAndFlush(veiculo);
        return veiculo;
    }

    @Override
    public NotaFiscal buscar(long id) {
        var nota = notas.findById(id);
        if (nota == null) throw new ResourceNotFoundException("Nota fiscal não encontrada.");
        return nota;
    }

    @Override
    @Transactional
    public void excluir(long id) {
        NotaFiscal nota = buscar(id);
        var veiculosDaNota = nota.getItens().stream()
                .map(ItemNotaFiscal::getProduto)
                .filter(Veiculo.class::isInstance)
                .map(Veiculo.class::cast)
                .toList();
        if (nota.isEntrada()) {
            for (Veiculo veiculo : veiculosDaNota) {
                long outrasReferencias = notas.getEntityManager().createQuery(
                        "select count(i) from ItemNotaFiscal i where i.produto.id = :produtoId and i.notaFiscal.id <> :notaId",
                        Long.class).setParameter("produtoId", veiculo.getId()).setParameter("notaId", id).getSingleResult();
                if (outrasReferencias > 0 || veiculo.getStatus() == StatusVeiculo.VENDIDO) {
                    throw new ResourceConflictException("A nota de entrada não pode ser excluída porque um de seus veículos já foi utilizado em outra nota.");
                }
            }
            notas.delete(nota);
            notas.getEntityManager().flush();
            veiculosDaNota.forEach(veiculos::delete);
        } else {
            veiculosDaNota.forEach(v -> v.setStatus(StatusVeiculo.DISPONIVEL));
            notas.delete(nota);
        }
    }
}
