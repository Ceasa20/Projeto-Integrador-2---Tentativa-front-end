package com.locadora;

import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class DadosLocadora {
    private static final String VERSAO_ARQUIVO = "1";
    private static final int LIMITE_REGISTROS = 100_000;
    private final Path arquivo;
    private final List<Veiculo> veiculos = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Locacao> locacoes = new ArrayList<>();
    private int proximoIdVeiculo = 1;
    private int proximoIdCliente = 1;
    private int proximoIdLocacao = 1;

    public DadosLocadora() throws IOException {
        this(Paths.get("dados-locadora.properties"));
    }

    public DadosLocadora(Path arquivo) throws IOException {
        this.arquivo = arquivo.toAbsolutePath().normalize();
        if (Files.exists(this.arquivo)) {
            carregar();
        } else {
            popularDadosDemonstracao();
            salvar();
        }
    }

    public List<Veiculo> getVeiculos() {
        return Collections.unmodifiableList(veiculos);
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Locacao> getLocacoes() {
        return Collections.unmodifiableList(locacoes);
    }

    public void salvar() throws IOException {
        Properties propriedades = new Properties();
        propriedades.setProperty("versao", VERSAO_ARQUIVO);
        propriedades.setProperty("veiculos.quantidade", String.valueOf(veiculos.size()));
        propriedades.setProperty("clientes.quantidade", String.valueOf(clientes.size()));
        propriedades.setProperty("locacoes.quantidade", String.valueOf(locacoes.size()));

        for (int i = 0; i < veiculos.size(); i++) {
            Veiculo veiculo = veiculos.get(i);
            String prefixo = "veiculos." + i + ".";
            propriedades.setProperty(prefixo + "id", String.valueOf(veiculo.id));
            propriedades.setProperty(prefixo + "marca", codificar(veiculo.marca));
            propriedades.setProperty(prefixo + "modelo", codificar(veiculo.modelo));
            propriedades.setProperty(prefixo + "ano", String.valueOf(veiculo.ano));
            propriedades.setProperty(prefixo + "placa", codificar(veiculo.placa));
            propriedades.setProperty(prefixo + "valorDiaria", veiculo.valorDiaria.toPlainString());
        }

        for (int i = 0; i < clientes.size(); i++) {
            Cliente cliente = clientes.get(i);
            String prefixo = "clientes." + i + ".";
            propriedades.setProperty(prefixo + "id", String.valueOf(cliente.id));
            propriedades.setProperty(prefixo + "nome", codificar(cliente.nome));
            propriedades.setProperty(prefixo + "cpf", codificar(cliente.cpf));
            propriedades.setProperty(prefixo + "telefone", codificar(cliente.telefone));
            propriedades.setProperty(prefixo + "email", codificar(cliente.email));
        }

        for (int i = 0; i < locacoes.size(); i++) {
            Locacao locacao = locacoes.get(i);
            String prefixo = "locacoes." + i + ".";
            propriedades.setProperty(prefixo + "id", String.valueOf(locacao.id));
            propriedades.setProperty(prefixo + "clienteId", String.valueOf(locacao.cliente.id));
            propriedades.setProperty(prefixo + "veiculoId", String.valueOf(locacao.veiculo.id));
            propriedades.setProperty(prefixo + "inicio", locacao.inicio.toString());
            propriedades.setProperty(prefixo + "fimPrevisto", locacao.fimPrevisto.toString());
            propriedades.setProperty(prefixo + "valorDiaria", locacao.valorDiaria.toPlainString());
            propriedades.setProperty(prefixo + "ativa", String.valueOf(locacao.ativa));
        }

        Path diretorio = arquivo.getParent();
        Files.createDirectories(diretorio);
        Path temporario = Files.createTempFile(diretorio, "dados-locadora-", ".tmp");
        boolean movido = false;
        try {
            try (OutputStream saida = Files.newOutputStream(temporario)) {
                propriedades.store(saida, "Dados da locadora");
            }
            try {
                Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
            movido = true;
        } finally {
            if (!movido) {
                Files.deleteIfExists(temporario);
            }
        }
    }

    private void carregar() throws IOException {
        if (!Files.exists(arquivo)) {
            return;
        }
        Properties propriedades = new Properties();
        try (InputStream entrada = Files.newInputStream(arquivo)) {
            propriedades.load(entrada);
        }
        if (!VERSAO_ARQUIVO.equals(propriedades.getProperty("versao"))) {
            throw new IOException("A versao do arquivo de dados nao e reconhecida: " + arquivo);
        }
        try {
            carregarRegistros(propriedades);
        } catch (IllegalArgumentException e) {
            throw new IOException("O arquivo de dados esta invalido: " + arquivo, e);
        }
    }

    private void popularDadosDemonstracao() {
        String[][] clientesExemplo = {
                {"Cliente Demonstracao 01", "90000000001"},
                {"Cliente Demonstracao 02", "90000000002"},
                {"Cliente Demonstracao 03", "90000000003"},
                {"Cliente Demonstracao 04", "90000000004"},
                {"Cliente Demonstracao 05", "90000000005"},
                {"Cliente Demonstracao 06", "90000000006"},
                {"Cliente Demonstracao 07", "90000000007"},
                {"Cliente Demonstracao 08", "90000000008"},
                {"Cliente Demonstracao 09", "90000000009"},
                {"Cliente Demonstracao 10", "90000000010"}
        };
        for (int i = 0; i < clientesExemplo.length; i++) {
            String telefone = String.format("2799000%04d", i + 1);
            cadastrarCliente(clientesExemplo[i][0], clientesExemplo[i][1], telefone,
                    String.format("cliente%02d@example.com", i + 1));
        }

        String[][] veiculosExemplo = {
                {"Ford", "Mustang GT", "2022", "DEMO001", "1290.00"},
                {"Chevrolet", "Camaro SS", "2021", "DEMO002", "1150.00"},
                {"Mitsubishi", "Lancer Evolution X", "2015", "DEMO003", "780.00"},
                {"Toyota", "Supra GR", "2023", "DEMO004", "1850.00"},
                {"Nissan", "Skyline GT-R R34", "2002", "DEMO005", "2250.00"},
                {"Nissan", "GT-R R35", "2021", "DEMO006", "2450.00"},
                {"Ferrari", "488 GTB", "2020", "DEMO007", "6200.00"},
                {"Ferrari", "F8 Tributo", "2022", "DEMO008", "7800.00"},
                {"Ferrari", "Portofino", "2021", "DEMO009", "6900.00"},
                {"Mercedes-Benz", "C 300", "2023", "DEMO010", "890.00"},
                {"Mercedes-Benz", "GLC 300", "2022", "DEMO011", "1190.00"},
                {"Mercedes-AMG", "GT", "2021", "DEMO012", "4950.00"}
        };
        for (String[] veiculo : veiculosExemplo) {
            cadastrarVeiculo(veiculo[0], veiculo[1], Integer.parseInt(veiculo[2]), veiculo[3],
                    new BigDecimal(veiculo[4]));
        }

        LocalDate hoje = LocalDate.now();
        criarLocacaoDemonstracao(0, 0, hoje.minusDays(15), hoje.minusDays(12), false);
        criarLocacaoDemonstracao(1, 1, hoje.minusDays(8), hoje.minusDays(5), false);
        criarLocacaoDemonstracao(2, 2, hoje.minusDays(5), hoje.minusDays(3), false);
        criarLocacaoDemonstracao(3, 3, hoje.minusDays(2), hoje.plusDays(2), true);
        criarLocacaoDemonstracao(4, 4, hoje.minusDays(1), hoje.plusDays(3), true);
        criarLocacaoDemonstracao(5, 5, hoje.minusDays(10), hoje.minusDays(9), false);
        criarLocacaoDemonstracao(6, 6, hoje, hoje.plusDays(1), true);
        criarLocacaoDemonstracao(7, 9, hoje.minusDays(20), hoje.minusDays(18), false);
    }

    private void criarLocacaoDemonstracao(
            int indiceCliente, int indiceVeiculo, LocalDate inicio, LocalDate fim, boolean ativa) {
        Locacao locacao = cadastrarLocacao(
                clientes.get(indiceCliente), veiculos.get(indiceVeiculo), inicio, fim);
        if (!ativa) {
            finalizarLocacao(locacao);
        }
    }

    private void carregarRegistros(Properties propriedades) {
        Map<Integer, Veiculo> veiculosPorId = new HashMap<>();
        Map<Integer, Cliente> clientesPorId = new HashMap<>();
        int quantidadeVeiculos = lerQuantidade(propriedades, "veiculos.quantidade");
        int quantidadeClientes = lerQuantidade(propriedades, "clientes.quantidade");
        int quantidadeLocacoes = lerQuantidade(propriedades, "locacoes.quantidade");

        for (int i = 0; i < quantidadeVeiculos; i++) {
            String prefixo = "veiculos." + i + ".";
            int id = lerId(propriedades, prefixo + "id");
            Veiculo veiculo = new Veiculo(id,
                    decodificar(obrigatorio(propriedades, prefixo + "marca")),
                    decodificar(obrigatorio(propriedades, prefixo + "modelo")),
                    Integer.parseInt(obrigatorio(propriedades, prefixo + "ano")),
                    decodificar(obrigatorio(propriedades, prefixo + "placa")),
                    validarValorDiaria(new BigDecimal(obrigatorio(propriedades, prefixo + "valorDiaria"))));
            validarTexto(veiculo.marca, "Marca");
            validarTexto(veiculo.modelo, "Modelo");
            validarAno(veiculo.ano);
            validarTexto(veiculo.placa, "Placa");
            if (veiculosPorId.put(id, veiculo) != null || buscarVeiculoPorPlaca(veiculo.placa, null) != null) {
                throw new IllegalArgumentException("Ha IDs ou placas duplicados no cadastro de veiculos.");
            }
            veiculos.add(veiculo);
            proximoIdVeiculo = Math.max(proximoIdVeiculo, proximoId(id));
        }

        for (int i = 0; i < quantidadeClientes; i++) {
            String prefixo = "clientes." + i + ".";
            int id = lerId(propriedades, prefixo + "id");
            Cliente cliente = new Cliente(id,
                    decodificar(obrigatorio(propriedades, prefixo + "nome")),
                    decodificar(obrigatorio(propriedades, prefixo + "cpf")),
                    decodificar(obrigatorio(propriedades, prefixo + "telefone")),
                    decodificar(obrigatorio(propriedades, prefixo + "email")));
            validarTexto(cliente.nome, "Nome");
            validarTexto(cliente.cpf, "CPF");
            validarTexto(cliente.telefone, "Telefone");
            validarCpf(cliente.cpf);
            if (clientesPorId.put(id, cliente) != null || buscarClientePorCpf(cliente.cpf, null) != null) {
                throw new IllegalArgumentException("Ha IDs ou CPFs duplicados no cadastro de clientes.");
            }
            clientes.add(cliente);
            proximoIdCliente = Math.max(proximoIdCliente, proximoId(id));
        }

        Map<Integer, Locacao> locacoesPorId = new HashMap<>();
        for (int i = 0; i < quantidadeLocacoes; i++) {
            String prefixo = "locacoes." + i + ".";
            int id = lerId(propriedades, prefixo + "id");
            Cliente cliente = clientesPorId.get(Integer.parseInt(
                    obrigatorio(propriedades, prefixo + "clienteId")));
            Veiculo veiculo = veiculosPorId.get(Integer.parseInt(
                    obrigatorio(propriedades, prefixo + "veiculoId")));
            if (cliente == null || veiculo == null) {
                throw new IllegalArgumentException("Uma locacao referencia cliente ou veiculo inexistente.");
            }
            LocalDate inicio = LocalDate.parse(obrigatorio(propriedades, prefixo + "inicio"));
            LocalDate fimPrevisto = LocalDate.parse(obrigatorio(propriedades, prefixo + "fimPrevisto"));
            if (fimPrevisto.isBefore(inicio)) {
                throw new IllegalArgumentException("Uma locacao possui datas invalidas.");
            }
            String ativaTexto = obrigatorio(propriedades, prefixo + "ativa");
            if (!"true".equals(ativaTexto) && !"false".equals(ativaTexto)) {
                throw new IllegalArgumentException("O status de uma locacao e invalido.");
            }
            BigDecimal valorDiaria = validarValorDiaria(
                    new BigDecimal(obrigatorio(propriedades, prefixo + "valorDiaria")));
            Locacao locacao = new Locacao(id, cliente, veiculo, inicio, fimPrevisto,
                    valorDiaria, Boolean.parseBoolean(ativaTexto));
            if (locacoesPorId.put(id, locacao) != null
                    || (locacao.ativa && temLocacaoAtiva(veiculo))) {
                throw new IllegalArgumentException("Ha IDs duplicados ou veiculos com locacoes ativas repetidas.");
            }
            locacoes.add(locacao);
            proximoIdLocacao = Math.max(proximoIdLocacao, proximoId(id));
        }
    }

    private int lerQuantidade(Properties propriedades, String chave) {
        int quantidade = Integer.parseInt(obrigatorio(propriedades, chave));
        if (quantidade < 0 || quantidade > LIMITE_REGISTROS) {
            throw new IllegalArgumentException("Quantidade invalida em " + chave + ".");
        }
        return quantidade;
    }

    private int lerId(Properties propriedades, String chave) {
        int id = Integer.parseInt(obrigatorio(propriedades, chave));
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido em " + chave + ".");
        }
        return id;
    }

    private int proximoId(int id) {
        if (id == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Um ID excede o limite permitido.");
        }
        return id + 1;
    }

    private String obrigatorio(Properties propriedades, String chave) {
        String valor = propriedades.getProperty(chave);
        if (valor == null) {
            throw new IllegalArgumentException("Campo ausente no arquivo de dados: " + chave);
        }
        return valor;
    }

    private String codificar(String valor) {
        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private String decodificar(String valor) {
        return new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8);
    }

    public Veiculo cadastrarVeiculo(String marca, String modelo, int ano, String placa, BigDecimal valorDiaria) {
        validarTexto(marca, "Marca");
        validarTexto(modelo, "Modelo");
        validarAno(ano);
        validarTexto(placa, "Placa");
        valorDiaria = validarValorDiaria(valorDiaria);
        if (buscarVeiculoPorPlaca(placa, null) != null) {
            throw new IllegalArgumentException("Ja existe um veiculo cadastrado com essa placa.");
        }
        Veiculo veiculo = new Veiculo(
                proximoIdVeiculo++, marca.trim(), modelo.trim(), ano, placa.trim(), valorDiaria);
        veiculos.add(veiculo);
        return veiculo;
    }

    public void alterarVeiculo(
            Veiculo veiculo, String marca, String modelo, int ano, String placa, BigDecimal valorDiaria) {
        validarVeiculoExistente(veiculo);
        validarTexto(marca, "Marca");
        validarTexto(modelo, "Modelo");
        validarAno(ano);
        validarTexto(placa, "Placa");
        valorDiaria = validarValorDiaria(valorDiaria);
        if (buscarVeiculoPorPlaca(placa, veiculo) != null) {
            throw new IllegalArgumentException("Ja existe um veiculo cadastrado com essa placa.");
        }
        veiculo.marca = marca.trim();
        veiculo.modelo = modelo.trim();
        veiculo.ano = ano;
        veiculo.placa = placa.trim();
        veiculo.valorDiaria = valorDiaria;
    }

    public void excluirVeiculo(Veiculo veiculo) {
        validarVeiculoExistente(veiculo);
        if (contarLocacoes(veiculo) > 0) {
            throw new IllegalArgumentException("Este veiculo possui locacoes no historico e nao pode ser excluido.");
        }
        veiculos.remove(veiculo);
    }

    public Cliente cadastrarCliente(String nome, String cpf, String telefone, String email) {
        validarTexto(nome, "Nome");
        validarTexto(cpf, "CPF");
        validarTexto(telefone, "Telefone");
        validarCpf(cpf);
        if (buscarClientePorCpf(cpf, null) != null) {
            throw new IllegalArgumentException("Ja existe um cliente cadastrado com esse CPF.");
        }
        Cliente cliente = new Cliente(proximoIdCliente++, nome.trim(), cpf.trim(), telefone.trim(), email.trim());
        clientes.add(cliente);
        return cliente;
    }

    public void alterarCliente(Cliente cliente, String nome, String cpf, String telefone, String email) {
        validarClienteExistente(cliente);
        validarTexto(nome, "Nome");
        validarTexto(cpf, "CPF");
        validarTexto(telefone, "Telefone");
        validarCpf(cpf);
        if (buscarClientePorCpf(cpf, cliente) != null) {
            throw new IllegalArgumentException("Ja existe um cliente cadastrado com esse CPF.");
        }
        cliente.nome = nome.trim();
        cliente.cpf = cpf.trim();
        cliente.telefone = telefone.trim();
        cliente.email = email.trim();
    }

    public void excluirCliente(Cliente cliente) {
        validarClienteExistente(cliente);
        if (contarLocacoes(cliente) > 0) {
            throw new IllegalArgumentException("Este cliente possui locacoes no historico e nao pode ser excluido.");
        }
        clientes.remove(cliente);
    }

    public List<Veiculo> getVeiculosDisponiveis() {
        List<Veiculo> disponiveis = new ArrayList<>();
        for (Veiculo veiculo : veiculos) {
            if (!temLocacaoAtiva(veiculo)) {
                disponiveis.add(veiculo);
            }
        }
        return Collections.unmodifiableList(disponiveis);
    }

    public Locacao cadastrarLocacao(Cliente cliente, Veiculo veiculo, LocalDate inicio, LocalDate fimPrevisto) {
        if (clientes.isEmpty() || veiculos.isEmpty()) {
            throw new IllegalArgumentException("Cadastre pelo menos um cliente e um veiculo antes de criar uma locacao.");
        }
        validarClienteExistente(cliente);
        validarVeiculoExistente(veiculo);
        if (inicio == null || fimPrevisto == null || fimPrevisto.isBefore(inicio)) {
            throw new IllegalArgumentException("Informe datas validas; o fim nao pode ser anterior ao inicio.");
        }
        if (temLocacaoAtiva(veiculo)) {
            throw new IllegalArgumentException("Este veiculo ja possui uma locacao ativa.");
        }
        Locacao locacao = new Locacao(proximoIdLocacao++, cliente, veiculo, inicio, fimPrevisto,
                veiculo.getValorDiaria(), true);
        locacoes.add(locacao);
        return locacao;
    }

    public void finalizarLocacao(Locacao locacao) {
        if (!locacoes.contains(locacao)) {
            throw new IllegalArgumentException("A locacao selecionada nao existe.");
        }
        if (!locacao.ativa) {
            throw new IllegalArgumentException("A locacao selecionada ja foi finalizada.");
        }
        locacao.ativa = false;
    }

    public int contarLocacoesAtivas() {
        int total = 0;
        for (Locacao locacao : locacoes) {
            if (locacao.ativa) {
                total++;
            }
        }
        return total;
    }

    public BigDecimal calcularFaturamentoMes(YearMonth mes) {
        if (mes == null) {
            throw new IllegalArgumentException("Informe o mes para calcular o faturamento.");
        }
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Locacao locacao : locacoes) {
            if (!locacao.ativa && YearMonth.from(locacao.fimPrevisto).equals(mes)) {
                total = total.add(locacao.valorTotal);
            }
        }
        return total;
    }

    public int contarLocacoes(Veiculo veiculo) {
        int total = 0;
        for (Locacao locacao : locacoes) {
            if (locacao.veiculo == veiculo) {
                total++;
            }
        }
        return total;
    }

    public int contarLocacoes(Cliente cliente) {
        int total = 0;
        for (Locacao locacao : locacoes) {
            if (locacao.cliente == cliente) {
                total++;
            }
        }
        return total;
    }

    public Map<String, Integer> contarLocacoesPorModelo() {
        Map<String, Integer> totais = new LinkedHashMap<>();
        for (Locacao locacao : locacoes) {
            String modelo = locacao.veiculo.modelo;
            totais.put(modelo, totais.getOrDefault(modelo, 0) + 1);
        }
        return Collections.unmodifiableMap(totais);
    }

    private boolean temLocacaoAtiva(Veiculo veiculo) {
        for (Locacao locacao : locacoes) {
            if (locacao.veiculo == veiculo && locacao.ativa) {
                return true;
            }
        }
        return false;
    }

    private Veiculo buscarVeiculoPorPlaca(String placa, Veiculo ignorar) {
        String normalizada = normalizarPlaca(placa);
        for (Veiculo veiculo : veiculos) {
            if (veiculo != ignorar && normalizarPlaca(veiculo.placa).equals(normalizada)) {
                return veiculo;
            }
        }
        return null;
    }

    private Cliente buscarClientePorCpf(String cpf, Cliente ignorar) {
        String normalizado = normalizarCpf(cpf);
        for (Cliente cliente : clientes) {
            if (cliente != ignorar && normalizarCpf(cliente.cpf).equals(normalizado)) {
                return cliente;
            }
        }
        return null;
    }

    private void validarVeiculoExistente(Veiculo veiculo) {
        if (!veiculos.contains(veiculo)) {
            throw new IllegalArgumentException("O veiculo selecionado nao existe mais.");
        }
    }

    private void validarClienteExistente(Cliente cliente) {
        if (!clientes.contains(cliente)) {
            throw new IllegalArgumentException("O cliente selecionado nao existe mais.");
        }
    }

    private void validarCpf(String cpf) {
        if (normalizarCpf(cpf).length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter 11 digitos.");
        }
    }

    private BigDecimal validarValorDiaria(BigDecimal valorDiaria) {
        if (valorDiaria == null) {
            throw new IllegalArgumentException("Informe o valor da diaria.");
        }
        BigDecimal valorArredondado = valorDiaria.setScale(2, RoundingMode.HALF_UP);
        if (valorArredondado.signum() <= 0) {
            throw new IllegalArgumentException("O valor da diaria deve ser maior que zero.");
        }
        return valorArredondado;
    }

    private void validarAno(int ano) {
        if (ano < 1900 || ano > LocalDate.now().getYear() + 1) {
            throw new IllegalArgumentException("Informe um ano valido para o veiculo.");
        }
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Preencha o campo " + campo + ".");
        }
    }

    private String normalizarPlaca(String placa) {
        return placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    public static final class Veiculo {
        private final int id;
        private String marca;
        private String modelo;
        private int ano;
        private String placa;
        private BigDecimal valorDiaria;

        private Veiculo(int id, String marca, String modelo, int ano, String placa, BigDecimal valorDiaria) {
            this.id = id;
            this.marca = marca;
            this.modelo = modelo;
            this.ano = ano;
            this.placa = placa;
            this.valorDiaria = valorDiaria;
        }

        public int getId() { return id; }
        public String getMarca() { return marca; }
        public String getModelo() { return modelo; }
        public int getAno() { return ano; }
        public String getPlaca() { return placa; }
        public BigDecimal getValorDiaria() { return valorDiaria; }

        @Override
        public String toString() {
            return modelo + " - " + placa;
        }
    }

    public static final class Cliente {
        private final int id;
        private String nome;
        private String cpf;
        private String telefone;
        private String email;

        private Cliente(int id, String nome, String cpf, String telefone, String email) {
            this.id = id;
            this.nome = nome;
            this.cpf = cpf;
            this.telefone = telefone;
            this.email = email;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getCpf() { return cpf; }
        public String getTelefone() { return telefone; }
        public String getEmail() { return email; }

        @Override
        public String toString() {
            return nome + " - " + cpf;
        }
    }

    public static final class Locacao {
        private final int id;
        private final Cliente cliente;
        private final Veiculo veiculo;
        private final LocalDate inicio;
        private final LocalDate fimPrevisto;
        private final BigDecimal valorDiaria;
        private final BigDecimal valorTotal;
        private boolean ativa = true;

        private Locacao(int id, Cliente cliente, Veiculo veiculo, LocalDate inicio, LocalDate fimPrevisto,
                BigDecimal valorDiaria, boolean ativa) {
            this.id = id;
            this.cliente = cliente;
            this.veiculo = veiculo;
            this.inicio = inicio;
            this.fimPrevisto = fimPrevisto;
            this.valorDiaria = valorDiaria;
            long quantidadeDiarias = ChronoUnit.DAYS.between(inicio, fimPrevisto) + 1;
            this.valorTotal = valorDiaria.multiply(BigDecimal.valueOf(quantidadeDiarias))
                    .setScale(2, RoundingMode.HALF_UP);
            this.ativa = ativa;
        }

        public int getId() { return id; }
        public Cliente getCliente() { return cliente; }
        public Veiculo getVeiculo() { return veiculo; }
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFimPrevisto() { return fimPrevisto; }
        public BigDecimal getValorDiaria() { return valorDiaria; }
        public BigDecimal getValorTotal() { return valorTotal; }
        public boolean isAtiva() { return ativa; }
    }
}
