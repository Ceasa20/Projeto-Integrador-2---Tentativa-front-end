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

As telas e componentes visuais ficam em `src/com/locadora/view`, seguindo a organizacao por pacote Java usada pelo repositorio de destino. `DadosLocadora.java` mantem somente o armazenamento temporario em memoria e pode ser substituido por servicos/DAOs quando a integracao com o banco for iniciada. Os icones permanecem em `assets/imagens`.

As tabelas exibem IDs temporarios atribuidos em memoria para preparar a interface para chaves primarias do banco. Eles reiniciam ao fechar o programa e deverao ser substituidos pelos IDs retornados pelo banco na integracao SQL.

Os relatorios incluem contagem de locacoes agrupadas por modelo e por cliente, alem de uma listagem detalhada que combina locacao, cliente e veiculo. Nesta versao, esses dados sao montados a partir dos objetos em memoria; as consultas SQL serao ligadas posteriormente.

## Compilar e executar no PowerShell

No terminal, na pasta do projeto:

```powershell
$fontes = Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName
javac -d out $fontes
java -cp out com.locadora.view.TelaPrincipal
```

Execute o comando `java` a partir da pasta raiz do projeto para que a aplicacao encontre `assets/imagens`.

Os dados ficam em memoria nesta versao e sao perdidos quando a aplicacao e encerrada.
