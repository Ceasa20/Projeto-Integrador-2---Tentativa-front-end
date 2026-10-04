package com.locadora.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import com.locadora.DadosLocadora;

public class TelaPrincipal extends JFrame {
    private static final Color NAVY = new Color(29, 47, 65);
    private static final Color BLUE = new Color(36, 91, 190);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BACKGROUND = new Color(247, 249, 252);

    private final DadosLocadora dados = new DadosLocadora();
    private final CardLayout layoutTelas = new CardLayout();
    private final JPanel telas = new JPanel(layoutTelas);
    private final JLabel tituloTela = new JLabel("Dashboard");
    private final JLabel totalVeiculos = new JLabel("0");
    private final JLabel totalClientes = new JLabel("0");
    private final JLabel totalLocacoes = new JLabel("0");
    private final List<JButton> botoesMenu = new ArrayList<>();
    private final Icon iconePainel;
    private final Icon iconeVeiculo;
    private final Icon iconeVeiculoMenu;
    private final Icon iconeCliente;
    private final Icon iconeLocacao;
    private final Icon iconeRelatorio;
    private final Icon iconeSair;
    private TelaVeiculos telaVeiculos;
    private TelaClientes telaClientes;
    private TelaLocacoes telaLocacoes;
    private TelaRelatorios telaRelatorios;

    public TelaPrincipal() {
        iconePainel = carregarIcone("painel.png", 20);
        iconeVeiculo = carregarIcone("carros.png", 32);
        iconeVeiculoMenu = carregarIcone("carros.png", 20);
        iconeCliente = carregarIcone("cliente.png", 20);
        iconeLocacao = carregarIcone("locacao.png", 20);
        iconeRelatorio = carregarIcone("relatorio-de-negocios.png", 20);
        iconeSair = carregarIcone("sair.png", 20);

        setTitle("Locadora de Veiculos - Dashboard");
        setSize(1150, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(criarMenuLateral(), BorderLayout.WEST);

        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);
        area.add(criarCabecalho(), BorderLayout.NORTH);
        telas.add(criarDashboard(), "Dashboard");
        telaVeiculos = new TelaVeiculos(dados, () -> mostrarTela("Dashboard", "Dashboard", 0));
        telaClientes = new TelaClientes(dados, () -> mostrarTela("Dashboard", "Dashboard", 0));
        telaLocacoes = new TelaLocacoes(dados, () -> mostrarTela("Dashboard", "Dashboard", 0));
        telaRelatorios = new TelaRelatorios(dados, () -> mostrarTela("Dashboard", "Dashboard", 0));
        telas.add(telaVeiculos, "Veiculos");
        telas.add(telaClientes, "Clientes");
        telas.add(telaLocacoes, "Locacoes");
        telas.add(telaRelatorios, "Relatorios");
        area.add(telas, BorderLayout.CENTER);
        root.add(area, BorderLayout.CENTER);
        add(root);
        atualizarDashboard();
    }

    private JPanel criarMenuLateral() {
        JPanel menu = new JPanel(new BorderLayout(0, 30));
        menu.setPreferredSize(new Dimension(190, 0));
        menu.setBackground(NAVY);
        menu.setBorder(BorderFactory.createEmptyBorder(28, 14, 24, 14));

        JLabel marca = new JLabel("<html><b style='font-size:15px'>LOCADORA</b><br><span style='font-size:11px'>DE VEICULOS</span></html>");
        marca.setIcon(new IconComFundo(iconeLocacao));
        marca.setIconTextGap(8);
        marca.setForeground(Color.WHITE);
        menu.add(marca, BorderLayout.NORTH);

        JPanel itens = new JPanel(new GridLayout(5, 1, 0, 8));
        itens.setOpaque(false);
        itens.add(criarBotaoMenu("Dashboard", iconePainel, 0, "Dashboard"));
        itens.add(criarBotaoMenu("Veiculos", iconeVeiculoMenu, 1, "Veiculos"));
        itens.add(criarBotaoMenu("Clientes", iconeCliente, 2, "Clientes"));
        itens.add(criarBotaoMenu("Locacoes", iconeLocacao, 3, "Locacoes"));
        itens.add(criarBotaoMenu("Relatorios", iconeRelatorio, 4, "Relatorios"));
        menu.add(itens, BorderLayout.CENTER);

        JButton sair = new JButton("Sair", new IconComFundo(iconeSair));
        sair.setIconTextGap(8);
        estilizarBotaoMenu(sair, false);
        sair.addActionListener(evento -> System.exit(0));
        menu.add(sair, BorderLayout.SOUTH);
        return menu;
    }

    private JButton criarBotaoMenu(String texto, Icon icone, int indice, String nomeTela) {
        JButton botao = new JButton(texto, new IconComFundo(icone));
        botao.setIconTextGap(8);
        estilizarBotaoMenu(botao, indice == 0);
        botao.addActionListener(evento -> mostrarTela(nomeTela, texto, indice));
        botoesMenu.add(botao);
        return botao;
    }

    private void estilizarBotaoMenu(JButton botao, boolean selecionado) {
        botao.setHorizontalAlignment(SwingConstants.LEFT);
        botao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        botao.setForeground(Color.WHITE);
        botao.setBackground(selecionado ? BLUE : NAVY);
        botao.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 4));
        botao.setFocusPainted(false);
        botao.setOpaque(true);
    }

    private void mostrarTela(String nomeTela, String titulo, int indiceMenu) {
        tituloTela.setText(titulo);
        for (int i = 0; i < botoesMenu.size(); i++) {
            botoesMenu.get(i).setBackground(i == indiceMenu ? BLUE : NAVY);
        }
        atualizarDashboard();
        telaVeiculos.atualizar();
        telaClientes.atualizar();
        telaLocacoes.atualizar();
        telaRelatorios.atualizar();
        layoutTelas.show(telas, nomeTela);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));
        tituloTela.setFont(new Font("SansSerif", Font.BOLD, 20));
        tituloTela.setForeground(TEXT);
        JLabel usuario = new JLabel("  (o)  Administrador  v");
        usuario.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usuario.setForeground(TEXT);
        cabecalho.add(tituloTela, BorderLayout.WEST);
        cabecalho.add(usuario, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setOpaque(false);
        JPanel conteudo = new JPanel(new BorderLayout(0, 22));
        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(28, 30, 30, 30));
        conteudo.add(criarBoasVindas(), BorderLayout.NORTH);
        conteudo.add(criarPainelPrincipal(), BorderLayout.CENTER);
        dashboard.add(conteudo, BorderLayout.CENTER);
        dashboard.add(criarRodapeCreditos(), BorderLayout.SOUTH);
        return dashboard;
    }

    private JPanel criarBoasVindas() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        JLabel titulo = new JLabel("Bem-vindo!");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(TEXT);
        JLabel subtitulo = new JLabel("Visao geral da locadora.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitulo.setForeground(MUTED);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(5));
        painel.add(subtitulo);
        return painel;
    }

    private JPanel criarPainelPrincipal() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(criarCard(iconeVeiculo, totalVeiculos, "Veiculos cadastrados", BLUE));
        cards.add(criarCard(iconeCliente, totalClientes, "Clientes cadastrados", new Color(42, 150, 88)));
        cards.add(criarCard(iconeLocacao, totalLocacoes, "Locacoes efetuadas", new Color(221, 145, 25)));
        cards.add(criarCard("$", "R$ 0,00", "Faturamento (mes)", new Color(126, 70, 185)));
        painel.add(cards, BorderLayout.NORTH);
        return painel;
    }

    private JPanel criarCard(Icon icone, JLabel valor, String legenda, Color cor) {
        return criarCard(new JLabel(icone, SwingConstants.CENTER), valor, legenda, cor);
    }

    private JPanel criarCard(String icone, String valor, String legenda, Color cor) {
        return criarCard(new JLabel(icone, SwingConstants.CENTER), new JLabel(valor), legenda, cor);
    }

    private JPanel criarCard(JLabel iconeLabel, JLabel valorLabel, String legenda, Color cor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(18, 16, 16, 16)));
        iconeLabel.setPreferredSize(new Dimension(48, 48));
        iconeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        iconeLabel.setForeground(cor);
        iconeLabel.setOpaque(true);
        iconeLabel.setBackground(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 24));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        valorLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valorLabel.setForeground(TEXT);
        JLabel legendaLabel = new JLabel(legenda);
        legendaLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        legendaLabel.setForeground(MUTED);
        textos.add(valorLabel);
        textos.add(Box.createVerticalStrut(5));
        textos.add(legendaLabel);
        card.add(iconeLabel, BorderLayout.WEST);
        card.add(textos, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarRodapeCreditos() {
        JPanel rodape = new JPanel();
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 30, 14, 30)));
        JLabel titulo = new JLabel("Informacoes do trabalho");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        titulo.setForeground(TEXT);
        rodape.add(titulo);
        rodape.add(Box.createVerticalStrut(5));
        rodape.add(criarLinhaCredito("Criado por: [Nomes dos integrantes]"));
        rodape.add(criarLinhaCredito("Disciplina: Banco de dados 2026/2"));
        rodape.add(criarLinhaCredito("Professor: Howard Roatti"));
        return rodape;
    }

    private JLabel criarLinhaCredito(String texto) {
        JLabel linha = new JLabel(texto);
        linha.setFont(new Font("SansSerif", Font.PLAIN, 11));
        linha.setForeground(MUTED);
        return linha;
    }

    private void atualizarDashboard() {
        totalVeiculos.setText(String.valueOf(dados.getVeiculos().size()));
        totalClientes.setText(String.valueOf(dados.getClientes().size()));
        totalLocacoes.setText(String.valueOf(dados.getLocacoes().size()));
    }

    private Icon carregarIcone(String nomeArquivo, int tamanho) {
        File arquivo = new File("assets/imagens", nomeArquivo);
        try {
            BufferedImage imagem = ImageIO.read(arquivo);
            if (imagem == null) {
                throw new IOException("Formato de imagem nao reconhecido.");
            }
            Image imagemRedimensionada = imagem.getScaledInstance(tamanho, tamanho, Image.SCALE_SMOOTH);
            return new ImageIcon(imagemRedimensionada);
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel carregar o icone: " + arquivo.getPath(), e);
        }
    }

    private static class IconComFundo implements Icon {
        private static final int PADDING = 4;
        private final Icon icone;

        private IconComFundo(Icon icone) {
            this.icone = icone;
        }

        @Override
        public int getIconWidth() {
            return icone.getIconWidth() + PADDING * 2;
        }

        @Override
        public int getIconHeight() {
            return icone.getIconHeight() + PADDING * 2;
        }

        @Override
        public void paintIcon(Component componente, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(x, y, getIconWidth(), getIconHeight(), 8, 8);
            icone.paintIcon(componente, g2, x + PADDING, y + PADDING);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
