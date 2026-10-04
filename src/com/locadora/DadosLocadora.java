package com.locadora;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DadosLocadora {
    private final List<Veiculo> veiculos = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Locacao> locacoes = new ArrayList<>();
    private int proximoIdVeiculo = 1;
    private int proximoIdCliente = 1;
    private int proximoIdLocacao = 1;

    public List<Veiculo> getVeiculos() {
        return Collections.unmodifiableList(veiculos);
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Locacao> getLocacoes() {
        return Collections.unmodifiableList(locacoes);
    }

    public Veiculo cadastrarVeiculo(String marca, String modelo, int ano, String placa) {
        validarTexto(marca, "Marca");
        validarTexto(modelo, "Modelo");
        validarAno(ano);
        validarTexto(placa, "Placa");
        if (buscarVeiculoPorPlaca(placa, null) != null) {
            throw new IllegalArgumentException("Ja existe um veiculo cadastrado com essa placa.");
        }
        Veiculo veiculo = new Veiculo(proximoIdVeiculo++, marca.trim(), modelo.trim(), ano, placa.trim());
        veiculos.add(veiculo);
        return veiculo;
    }

    public void alterarVeiculo(Veiculo veiculo, String marca, String modelo, int ano, String placa) {
        validarVeiculoExistente(veiculo);
        validarTexto(marca, "Marca");
        validarTexto(modelo, "Modelo");
        validarAno(ano);
        validarTexto(placa, "Placa");
        if (buscarVeiculoPorPlaca(placa, veiculo) != null) {
            throw new IllegalArgumentException("Ja existe um veiculo cadastrado com essa placa.");
        }
        veiculo.marca = marca.trim();
        veiculo.modelo = modelo.trim();
        veiculo.ano = ano;
        veiculo.placa = placa.trim();
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
        Locacao locacao = new Locacao(proximoIdLocacao++, cliente, veiculo, inicio, fimPrevisto);
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

        private Veiculo(int id, String marca, String modelo, int ano, String placa) {
            this.id = id;
            this.marca = marca;
            this.modelo = modelo;
            this.ano = ano;
            this.placa = placa;
        }

        public int getId() { return id; }
        public String getMarca() { return marca; }
        public String getModelo() { return modelo; }
        public int getAno() { return ano; }
        public String getPlaca() { return placa; }

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
        private boolean ativa = true;

        private Locacao(int id, Cliente cliente, Veiculo veiculo, LocalDate inicio, LocalDate fimPrevisto) {
            this.id = id;
            this.cliente = cliente;
            this.veiculo = veiculo;
            this.inicio = inicio;
            this.fimPrevisto = fimPrevisto;
        }

        public int getId() { return id; }
        public Cliente getCliente() { return cliente; }
        public Veiculo getVeiculo() { return veiculo; }
        public LocalDate getInicio() { return inicio; }
        public LocalDate getFimPrevisto() { return fimPrevisto; }
        public boolean isAtiva() { return ativa; }
    }
}
