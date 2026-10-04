package com.locadora.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import com.locadora.DadosLocadora;

public final class TelaRelatorios extends JPanel {
    private final DadosLocadora dados;
    private final JLabel resumo = ComponentesTela.criarResumo();
    private final DefaultTableModel modeloVeiculos = ComponentesTela.criarModelo("Modelo", "Locacoes");
    private final DefaultTableModel modeloClientes = ComponentesTela.criarModelo("ID cliente", "Cliente", "CPF", "Locacoes");
    private final DefaultTableModel modeloDetalhes = ComponentesTela.criarModelo(
            "ID locacao", "ID cliente", "Cliente", "ID veiculo", "Veiculo", "Inicio", "Previsao de fim", "Status");

    public TelaRelatorios(DadosLocadora dados, Runnable voltar) {
        this.dados = dados;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        add(ComponentesTela.criarCabecalho(
                "Relatorio agrupado por modelo, por cliente e detalhamento com dados relacionados.", resumo),
                BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Locacoes por modelo (agrupado)",
                new JScrollPane(ComponentesTela.criarTabela(modeloVeiculos)));
        abas.addTab("Locacoes por cliente",
                new JScrollPane(ComponentesTela.criarTabela(modeloClientes)));
        abas.addTab("Detalhes (cliente + veiculo)",
                new JScrollPane(ComponentesTela.criarTabela(modeloDetalhes)));
        add(abas, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acoes.setOpaque(false);
        acoes.add(ComponentesTela.criarBotao("Atualizar relatorios", this::atualizar, true));
        acoes.add(ComponentesTela.criarBotao("Voltar", voltar, false));
        add(acoes, BorderLayout.SOUTH);
        atualizar();
    }

    public void atualizar() {
        int ativas = dados.contarLocacoesAtivas();
        resumo.setText("Total de locacoes: " + dados.getLocacoes().size()
                + "    |    Ativas: " + ativas
                + "    |    Finalizadas: " + (dados.getLocacoes().size() - ativas));

        modeloVeiculos.setRowCount(0);
        for (Map.Entry<String, Integer> entrada : dados.contarLocacoesPorModelo().entrySet()) {
            modeloVeiculos.addRow(new Object[]{entrada.getKey(), entrada.getValue()});
        }
        modeloClientes.setRowCount(0);
        for (DadosLocadora.Cliente cliente : dados.getClientes()) {
            modeloClientes.addRow(new Object[]{
                    cliente.getId(),
                    cliente.getNome(),
                    cliente.getCpf(),
                    dados.contarLocacoes(cliente)
            });
        }
        modeloDetalhes.setRowCount(0);
        for (DadosLocadora.Locacao locacao : dados.getLocacoes()) {
            DadosLocadora.Cliente cliente = locacao.getCliente();
            DadosLocadora.Veiculo veiculo = locacao.getVeiculo();
            modeloDetalhes.addRow(new Object[]{
                    locacao.getId(),
                    cliente.getId(),
                    cliente.getNome(),
                    veiculo.getId(),
                    veiculo.getMarca() + " " + veiculo.getModelo() + " - " + veiculo.getPlaca(),
                    locacao.getInicio(),
                    locacao.getFimPrevisto(),
                    locacao.isAtiva() ? "Ativa" : "Finalizada"
            });
        }
    }
}
