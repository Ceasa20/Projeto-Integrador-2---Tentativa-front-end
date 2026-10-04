package com.locadora.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.locadora.DadosLocadora;

public final class TelaVeiculos extends JPanel {
    private final DadosLocadora dados;
    private final JLabel resumo = ComponentesTela.criarResumo();
    private final DefaultTableModel modelo = ComponentesTela.criarModelo("ID", "Marca", "Modelo", "Ano", "Placa");
    private final JTable tabela = ComponentesTela.criarTabela(modelo);

    public TelaVeiculos(DadosLocadora dados, Runnable voltar) {
        this.dados = dados;
        setLayout(new BorderLayout());
        setOpaque(false);
        add(ComponentesTela.criarPainelCadastro(
                "Cadastre e mantenha os veiculos da locadora.",
                resumo,
                tabela,
                ComponentesTela.criarBotao("Cadastrar", this::cadastrar, true),
                ComponentesTela.criarBotao("Alterar", this::alterar, false),
                ComponentesTela.criarBotao("Excluir", this::excluir, false),
                ComponentesTela.criarBotao("Voltar", voltar, false)), BorderLayout.CENTER);
        atualizar();
    }

    public void atualizar() {
        modelo.setRowCount(0);
        for (DadosLocadora.Veiculo veiculo : dados.getVeiculos()) {
            modelo.addRow(new Object[]{
                    veiculo.getId(), veiculo.getMarca(), veiculo.getModelo(), veiculo.getAno(), veiculo.getPlaca()
            });
        }
        resumo.setText("Veiculos cadastrados: " + dados.getVeiculos().size());
    }

    private void cadastrar() {
        String[] valores = ComponentesTela.abrirFormulario(this, "Cadastrar veiculo",
                new String[]{"Marca", "Modelo", "Ano", "Placa"}, null);
        if (valores == null) {
            return;
        }
        try {
            dados.cadastrarVeiculo(valores[0], valores[1], Integer.parseInt(valores[2]), valores[3]);
            atualizar();
        } catch (NumberFormatException e) {
            ComponentesTela.mostrarErro(this, "O ano deve ser informado como numero.");
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private void alterar() {
        DadosLocadora.Veiculo veiculo = selecionado();
        if (veiculo == null) {
            ComponentesTela.mostrarErro(this, "Selecione um veiculo para alterar.");
            return;
        }
        String[] valores = ComponentesTela.abrirFormulario(this, "Alterar veiculo",
                new String[]{"Marca", "Modelo", "Ano", "Placa"},
                new String[]{veiculo.getMarca(), veiculo.getModelo(), String.valueOf(veiculo.getAno()), veiculo.getPlaca()});
        if (valores == null) {
            return;
        }
        try {
            dados.alterarVeiculo(veiculo, valores[0], valores[1], Integer.parseInt(valores[2]), valores[3]);
            atualizar();
        } catch (NumberFormatException e) {
            ComponentesTela.mostrarErro(this, "O ano deve ser informado como numero.");
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private void excluir() {
        DadosLocadora.Veiculo veiculo = selecionado();
        if (veiculo == null) {
            ComponentesTela.mostrarErro(this, "Selecione um veiculo para excluir.");
            return;
        }
        if (!ComponentesTela.confirmarExclusao(this, "Deseja excluir o veiculo selecionado?")) {
            return;
        }
        try {
            dados.excluirVeiculo(veiculo);
            atualizar();
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private DadosLocadora.Veiculo selecionado() {
        int indice = tabela.getSelectedRow();
        return indice < 0 ? null : dados.getVeiculos().get(indice);
    }
}
