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
│           ├── database/
│           │   └── OracleConnectionFactory.java
│           └── view/
│               ├── ComponentesTela.java
│               ├── TelaPrincipal.java
│               ├── TelaVeiculos.java
│               ├── TelaClientes.java
│               ├── TelaLocacoes.java
│               └── TelaRelatorios.java
├── sql/
    └── criar_tabelas.sql
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

## Preparacao para Oracle JDBC

Este projeto inclui Maven e o driver Oracle JDBC (`ojdbc11`). A classe `com.locadora.database.OracleConnectionFactory` abre conexoes JDBC usando as variaveis de ambiente `ORACLE_JDBC_URL`, `ORACLE_USER` e `ORACLE_PASSWORD`; nenhuma credencial fica gravada no codigo.

No PowerShell, configure os valores da sua conexao Oracle antes de executar o programa:

```powershell
$env:ORACLE_JDBC_URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1"
$env:ORACLE_USER = "seu_usuario"
$env:ORACLE_PASSWORD = "sua_senha"
mvn compile
mvn exec:java
```

Substitua host, porta, nome do servico e credenciais pelos dados da conexao que funciona no DBeaver. O driver e baixado pelo Maven. Para executar `mvn`, instale o Maven e tenha acesso ao repositorio de dependencias; em uma VM sem internet, o Maven e o driver precisam estar previamente disponiveis.

**A conexao ainda nao esta ligada as telas nem substitui o armazenamento em arquivo.** O programa continua usando `dados-locadora.properties`; esta preparacao apenas adiciona o driver e a fabrica de conexoes para a etapa seguinte, quando o esquema Oracle estiver definido. As operacoes de cadastro devem ser migradas gradualmente para DAOs com `PreparedStatement`. Scripts de criacao de tabelas devem ser executados explicitamente; nao execute `DROP TABLE` ao iniciar o programa.

### Criar o esquema no DBeaver

O script [sql/criar_tabelas.sql](sql/criar_tabelas.sql) cria `CLIENTES`, `VEICULOS` e `LOCACOES`, incluindo chaves, validacoes e indices. No DBeaver, abra a conexao Oracle que sera usada pela aplicacao, abra o arquivo no editor SQL e use **Execute SQL Script**. Ele cria as tabelas no schema do usuario conectado; esse usuario precisa ter permissao para criar tabelas e indices.

Execute o script uma vez em um schema sem essas tabelas. Ele intencionalmente nao apaga objetos existentes: executa-lo novamente sem limpar ou migrar o schema resulta em erro de nomes ja existentes. Nao use `DROP` em um banco com dados que deseja preservar. A coluna `VALOR_DIARIA` da locacao registra a tarifa contratada na criacao; o total previsto pode ser calculado como tarifa multiplicada pela quantidade de dias inclusivos. A restricao de indice funcional permite apenas uma locacao ativa por veiculo.

Depois da criacao, o programa ainda precisa receber DAOs para trocar, por etapas, a persistencia local por operacoes JDBC. A senha e os dados da conexao nao devem ser escritos no script nem versionados.
