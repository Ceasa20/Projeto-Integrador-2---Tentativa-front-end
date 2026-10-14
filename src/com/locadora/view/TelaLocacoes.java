package com.locadora.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.locadora.DadosLocadora;

public final class TelaLocacoes extends JPanel {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DadosLocadora dados;
    private final JLabel resumo = ComponentesTela.criarResumo();
    private final DefaultTableModel modelo = ComponentesTela.criarModelo(
            "ID", "Cliente", "Veiculo", "Inicio", "Previsao de fim", "Diaria", "Total previsto", "Status");
    private final JTable tabela = ComponentesTela.criarTabela(modelo);

    public TelaLocacoes(DadosLocadora dados, Runnable voltar) {
        this.dados = dados;
        setLayout(new BorderLayout());
        setOpaque(false);
        add(ComponentesTela.criarPainelCadastro(
                "Cadastre clientes e veiculos primeiro. Um veiculo nao pode ter mais de uma locacao ativa.",
                resumo,
                tabela,
                ComponentesTela.criarBotao("Cadastrar locacao", this::cadastrar, true),
                ComponentesTela.criarBotao("Finalizar locacao", this::finalizar, false),
                ComponentesTela.criarBotao("Voltar", voltar, false)), BorderLayout.CENTER);
        atualizar();
    }

    public void atualizar() {
        modelo.setRowCount(0);
        for (DadosLocadora.Locacao locacao : dados.getLocacoes()) {
            modelo.addRow(new Object[]{
                    locacao.getId(),
                    locacao.getCliente().getNome(),
                    locacao.getVeiculo().getModelo() + " - " + locacao.getVeiculo().getPlaca(),
                    FORMATO_DATA.format(locacao.getInicio()),
                    FORMATO_DATA.format(locacao.getFimPrevisto()),
                    ComponentesTela.formatarMoeda(locacao.getValorDiaria()),
                    ComponentesTela.formatarMoeda(locacao.getValorTotal()),
                    locacao.isAtiva() ? "Ativa" : "Finalizada"
            });
        }
        resumo.setText("Locacoes ativas: " + dados.contarLocacoesAtivas()
                + "    |    Total efetuadas: " + dados.getLocacoes().size());
    }

    private void cadastrar() {
        if (dados.getClientes().isEmpty() || dados.getVeiculos().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cadastre pelo menos um cliente e um veiculo antes de cadastrar uma locacao.",
                    "Pre-requisitos da locacao", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (dados.getVeiculosDisponiveis().isEmpty()) {
            ComponentesTela.mostrarErro(this, "Nao ha veiculos disponiveis. Finalize uma locacao ativa primeiro.");
            return;
        }

        JComboBox<DadosLocadora.Cliente> clientes = new JComboBox<>(
                dados.getClientes().toArray(new DadosLocadora.Cliente[0]));
        JComboBox<DadosLocadora.Veiculo> veiculos = new JComboBox<>(
                dados.getVeiculosDisponiveis().toArray(new DadosLocadora.Veiculo[0]));
        veiculos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean selecionado, boolean foco) {
                JLabel rotulo = (JLabel) super.getListCellRendererComponent(
                        lista, valor, indice, selecionado, foco);
                if (valor instanceof DadosLocadora.Veiculo) {
                    DadosLocadora.Veiculo veiculo = (DadosLocadora.Veiculo) valor;
                    rotulo.setText(veiculo.getModelo() + " - " + veiculo.getPlaca() + " ("
                            + ComponentesTela.formatarMoeda(veiculo.getValorDiaria()) + "/dia)");
                }
                return rotulo;
            }
        });
        JTextField inicio = new JTextField(FORMATO_DATA.format(LocalDate.now()), 12);
        JTextField fim = new JTextField(FORMATO_DATA.format(LocalDate.now().plusDays(1)), 12);
        JPanel formulario = new JPanel(new GridLayout(4, 2, 8, 8));
        formulario.add(new JLabel("Cliente:"));
        formulario.add(clientes);
        formulario.add(new JLabel("Veiculo disponivel:"));
        formulario.add(veiculos);
        formulario.add(new JLabel("Inicio (dd/MM/yyyy):"));
        formulario.add(inicio);
        formulario.add(new JLabel("Previsao de fim (dd/MM/yyyy):"));
        formulario.add(fim);

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario, "Cadastrar locacao", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            LocalDate dataInicio = LocalDate.parse(inicio.getText().trim(), FORMATO_DATA);
            LocalDate dataFim = LocalDate.parse(fim.getText().trim(), FORMATO_DATA);
            dados.cadastrarLocacao((DadosLocadora.Cliente) clientes.getSelectedItem(),
                    (DadosLocadora.Veiculo) veiculos.getSelectedItem(), dataInicio, dataFim);
            atualizar();
        } catch (DateTimeParseException e) {
            ComponentesTela.mostrarErro(this, "Informe as datas no formato dd/MM/yyyy.");
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private void finalizar() {
        int indice = tabela.getSelectedRow();
        if (indice < 0) {
            ComponentesTela.mostrarErro(this, "Selecione uma locacao para finalizar.");
            return;
        }
        try {
            dados.finalizarLocacao(dados.getLocacoes().get(indice));
            atualizar();
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }
}
