package com.locadora.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.locadora.DadosLocadora;

public final class TelaClientes extends JPanel {
    private final DadosLocadora dados;
    private final JLabel resumo = ComponentesTela.criarResumo();
    private final DefaultTableModel modelo = ComponentesTela.criarModelo("ID", "Nome", "CPF", "Telefone", "E-mail");
    private final JTable tabela = ComponentesTela.criarTabela(modelo);

    public TelaClientes(DadosLocadora dados, Runnable voltar) {
        this.dados = dados;
        setLayout(new BorderLayout());
        setOpaque(false);
        add(ComponentesTela.criarPainelCadastro(
                "Cadastre e mantenha os clientes da locadora.",
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
        for (DadosLocadora.Cliente cliente : dados.getClientes()) {
            modelo.addRow(new Object[]{
                    cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getTelefone(), cliente.getEmail()
            });
        }
        resumo.setText("Clientes cadastrados: " + dados.getClientes().size());
    }

    private void cadastrar() {
        String[] valores = ComponentesTela.abrirFormulario(this, "Cadastrar cliente",
                new String[]{"Nome", "CPF", "Telefone", "E-mail"}, null);
        if (valores == null) {
            return;
        }
        try {
            dados.cadastrarCliente(valores[0], valores[1], valores[2], valores[3]);
            atualizar();
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private void alterar() {
        DadosLocadora.Cliente cliente = selecionado();
        if (cliente == null) {
            ComponentesTela.mostrarErro(this, "Selecione um cliente para alterar.");
            return;
        }
        String[] valores = ComponentesTela.abrirFormulario(this, "Alterar cliente",
                new String[]{"Nome", "CPF", "Telefone", "E-mail"},
                new String[]{cliente.getNome(), cliente.getCpf(), cliente.getTelefone(), cliente.getEmail()});
        if (valores == null) {
            return;
        }
        try {
            dados.alterarCliente(cliente, valores[0], valores[1], valores[2], valores[3]);
            atualizar();
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private void excluir() {
        DadosLocadora.Cliente cliente = selecionado();
        if (cliente == null) {
            ComponentesTela.mostrarErro(this, "Selecione um cliente para excluir.");
            return;
        }
        if (!ComponentesTela.confirmarExclusao(this, "Deseja excluir o cliente selecionado?")) {
            return;
        }
        try {
            dados.excluirCliente(cliente);
            atualizar();
        } catch (IllegalArgumentException e) {
            ComponentesTela.mostrarErro(this, e.getMessage());
        }
    }

    private DadosLocadora.Cliente selecionado() {
        int indice = tabela.getSelectedRow();
        return indice < 0 ? null : dados.getClientes().get(indice);
    }
}
