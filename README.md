# Locadora de Veiculos

Aplicacao desktop em Java Swing para gerenciar veiculos, clientes e locacoes.

## Estrutura do projeto

```text
.
├── assets/
│   └── imagens/
├── src/
│   └── com/
│       └── locadora/
│           ├── DadosLocadora.java
│           └── view/
│               ├── ComponentesTela.java
│               ├── TelaPrincipal.java
│               ├── TelaVeiculos.java
│               ├── TelaClientes.java
│               ├── TelaLocacoes.java
│               └── TelaRelatorios.java
└── README.md
```

As telas e componentes visuais ficam em `src/com/locadora/view`, seguindo a organizacao por pacote Java usada pelo repositorio de destino. Os icones permanecem em `assets/imagens`.

Na primeira execucao, o programa cria dados ficticios de demonstracao: clientes de teste, carros esportivos e de luxo (Mustang, Camaro, Lancer Evolution, Supra, Skyline, GT-R, Ferrari e Mercedes-Benz) e locacoes. Esses exemplos sao carregados somente se o arquivo de dados ainda nao existir. Os dados sao salvos no arquivo `dados-locadora.properties` na pasta de execucao e carregados nas execucoes seguintes; o arquivo e atualizado de forma atomica e os IDs sao mantidos.

Cada veiculo tem um valor de diaria editavel, com sugestao inicial de R$ 150,00. O valor total previsto da locacao e calculado pela quantidade de dias (contando inicio e fim) e fica registrado nos detalhes da locacao.

O card de faturamento mensal soma os valores das locacoes finalizadas cuja previsao de fim esta no mes atual. Locacoes ainda ativas nao entram no faturamento.

Os relatorios incluem contagem de locacoes agrupadas por modelo e por cliente, alem de uma listagem detalhada que combina locacao, cliente e veiculo. Nesta versao, esses dados sao montados a partir dos objetos em memoria; as consultas SQL serao ligadas posteriormente.

## Compilar e executar no PowerShell

No terminal, na pasta do projeto:

```powershell
$fontes = Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName
javac -d out $fontes
java -cp out com.locadora.view.TelaPrincipal
```

Execute o comando `java` a partir da pasta raiz do projeto para que a aplicacao encontre `assets/imagens`.

O arquivo `dados-locadora.properties` e criado automaticamente na pasta raiz quando o programa e fechado. Mantenha esse arquivo para preservar os cadastros e o historico entre execucoes.
