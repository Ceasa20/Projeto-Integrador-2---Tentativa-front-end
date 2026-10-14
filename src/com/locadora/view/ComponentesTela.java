package com.locadora.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;
import java.math.BigDecimal;

final class ComponentesTela {
    static final Color BLUE = new Color(36, 91, 190);
    static final Color TEXT = new Color(30, 41, 59);
    static final Color MUTED = new Color(100, 116, 139);

    private ComponentesTela() {
    }

    static DefaultTableModel criarModelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    static JTable criarTabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(34);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabela.setForeground(TEXT);
        tabela.setGridColor(new Color(226, 232, 240));
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabela.getTableHeader().setForeground(TEXT);
        tabela.getTableHeader().setBackground(new Color(241, 245, 249));
        return tabela;
    }

    static JButton criarBotao(String texto, Runnable acao, boolean principal) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("SansSerif", Font.BOLD, 13));
        botao.setForeground(principal ? Color.WHITE : TEXT);
        botao.setBackground(principal ? BLUE : Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(principal ? BLUE : new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        botao.addActionListener(evento -> acao.run());
        return botao;
    }

    static JPanel criarPainelCadastro(String descricao, JLabel resumo, JTable tabela, JButton... botoes) {
        JPanel painel = new JPanel(new BorderLayout(0, 16));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        painel.add(criarCabecalho(descricao, resumo), BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acoes.setOpaque(false);
        for (JButton botao : botoes) {
            acoes.add(botao);
        }
        painel.add(acoes, BorderLayout.SOUTH);
        return painel;
    }

    static JPanel criarCabecalho(String descricao, JLabel resumo) {
        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        JLabel textoDescricao = new JLabel(descricao);
        textoDescricao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textoDescricao.setForeground(MUTED);
        resumo.setFont(new Font("SansSerif", Font.BOLD, 14));
        resumo.setForeground(TEXT);
        cabecalho.add(textoDescricao);
        cabecalho.add(Box.createVerticalStrut(9));
        cabecalho.add(resumo);
        return cabecalho;
    }

    static JLabel criarResumo() {
        return new JLabel();
    }

    static String[] abrirFormulario(Component pai, String titulo, String[] rotulos, String[] valores) {
        JPanel formulario = new JPanel(new GridLayout(rotulos.length, 2, 8, 8));
        JTextField[] campos = new JTextField[rotulos.length];
        for (int i = 0; i < rotulos.length; i++) {
            formulario.add(new JLabel(rotulos[i] + ":"));
            campos[i] = new JTextField(valores == null ? "" : valores[i], 22);
            formulario.add(campos[i]);
        }
        int resultado = JOptionPane.showConfirmDialog(
                pai, formulario, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }
        String[] respostas = new String[campos.length];
        for (int i = 0; i < campos.length; i++) {
            respostas[i] = campos[i].getText().trim();
        }
        return respostas;
    }

    static void abrirFormularioComConfirmacao(
            Component pai, String titulo, String[] rotulos, String[] valores, Consumer<String[]> confirmar) {
        JPanel formulario = new JPanel(new GridLayout(rotulos.length, 2, 8, 8));
        JTextField[] campos = new JTextField[rotulos.length];
        for (int i = 0; i < rotulos.length; i++) {
            formulario.add(new JLabel(rotulos[i] + ":"));
            campos[i] = new JTextField(valores == null ? "" : valores[i], 22);
            formulario.add(campos[i]);
        }

        Window janelaPai = SwingUtilities.getWindowAncestor(pai);
        JDialog dialogo = new JDialog(janelaPai, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel conteudo = new JPanel(new BorderLayout(0, 12));
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        conteudo.add(formulario, BorderLayout.CENTER);

        JButton botaoConfirmar = new JButton("OK");
        JButton botaoCancelar = new JButton("Cancelar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(botaoConfirmar);
        botoes.add(botaoCancelar);
        conteudo.add(botoes, BorderLayout.SOUTH);

        botaoConfirmar.addActionListener(evento -> {
            String[] respostas = new String[campos.length];
            for (int i = 0; i < campos.length; i++) {
                respostas[i] = campos[i].getText().trim();
            }
            try {
                confirmar.accept(respostas);
                dialogo.dispose();
            } catch (IllegalArgumentException e) {
                mostrarErro(dialogo, e.getMessage());
            }
        });
        botaoCancelar.addActionListener(evento -> dialogo.dispose());

        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialogo.setContentPane(conteudo);
        dialogo.getRootPane().setDefaultButton(botaoConfirmar);
        dialogo.pack();
        dialogo.setLocationRelativeTo(pai);
        dialogo.setVisible(true);
    }

    static String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static void mostrarErro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atencao", JOptionPane.WARNING_MESSAGE);
    }

    static boolean confirmarExclusao(Component pai, String pergunta) {
        return JOptionPane.showConfirmDialog(pai, pergunta, "Confirmar exclusao",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
