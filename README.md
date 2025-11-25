# Projeto Confeitaria

Sistema de gestão para uma confeitaria, desenvolvido como parte do projeto integrador. A aplicação web permite o gerenciamento de ponta a ponta, desde o estoque de matéria-prima até o registro de vendas.

## ✨ Funcionalidades

- **Gestão de Usuários:** Cadastro e autenticação de usuários com diferentes níveis de acesso.
- **Controle de Estoque:** Entrada e monitoramento de matéria-prima.
- **Gestão de Produtos:** Cadastro de produtos acabados com seus ingredientes.
- **Pedidos e Orçamentos:** Criação e acompanhamento de pedidos e orçamentos para clientes.
- **Controle de Produção:** Registro da produção diária de itens.
- **Vendas:** Lançamento de novas vendas e geração de relatórios.
- **Segurança:** Autenticação de usuários e proteção de rotas.

## 🛠️ Tecnologias Utilizadas

- **Backend:**
  - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
  - [Spring Boot 3.5.7](https://spring.io/projects/spring-boot)
  - [Spring Web](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
  - [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
  - [Spring Security](https://spring.io/projects/spring-security)
  - [Maven](https://maven.apache.org/)
- **Frontend:**
  - [Thymeleaf](https://www.thymeleaf.org/)
  - HTML / CSS / JavaScript
- **Banco de Dados:**
  - [MySQL](https://www.mysql.com/)

## 🚀 Como Executar o Projeto

Siga os passos abaixo para configurar e executar o projeto em seu ambiente local.

### Pré-requisitos

- [Java JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) ou superior.
- [Apache Maven](https://maven.apache.org/download.cgi) instalado e configurado.
- [MySQL Server](https://dev.mysql.com/downloads/mysql/) instalado e em execução.

### 1. Clone o Repositório

```bash
git clone https://github.com/ryannnnnnnnnns/Confeitaria.git
```

### 2. Acesse a Pasta do Projeto

**Importante:** Antes de executar os próximos comandos, certifique-se de que seu terminal está operando dentro da pasta do projeto.

```bash
cd Confeitaria
```

### 3. Configure o Banco de Dados

1. Crie um banco de dados no seu MySQL com o nome `projeto`.
   ```sql
   CREATE DATABASE projeto;
   ```
2. Abra o arquivo `src/main/resources/application.properties` e altere as seguintes propriedades com suas credenciais do MySQL:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/projeto
   spring.datasource.username=seu_usuario_mysql
   spring.datasource.password=sua_senha_mysql
   ```
   > **Nota:** O `spring.jpa.hibernate.ddl-auto=update` irá gerar o esquema do banco de dados automaticamente na primeira vez que a aplicação for iniciada.

### 4. Compile o Projeto

Use o Maven Wrapper para compilar e instalar as dependências.

**No Windows:**
```bash
mvnw.cmd clean install
```

**No Linux ou macOS:**
```bash
./mvnw clean install
```

### 5. Execute a Aplicação

Você pode executar a aplicação usando o plugin do Spring Boot:

**No Windows:**
```bash
mvnw.cmd spring-boot:run
```

**No Linux ou macOS:**
```bash
./mvnw spring-boot:run
```

### 6. Acesse a Aplicação

Após a inicialização, abra seu navegador e acesse:

[http://localhost:8080](http://localhost:8080)

## 📂 Estrutura do Projeto

```
.
├── src
│   ├── main
│   │   ├── java/com/ifsp/projeto   # Código fonte da aplicação
│   │   │   ├── controller          # Controladores Spring MVC
│   │   │   ├── model               # Entidades JPA
│   │   │   ├── repository          # Repositórios Spring Data
│   │   │   └── service             # Lógica de negócio
│   │   └── resources
│   │       ├── static              # Arquivos estáticos (CSS, JS, imagens)
│   │       ├── templates           # Templates Thymeleaf (HTML)
│   │       └── application.properties # Configurações da aplicação
│   └── test                        # Testes da aplicação
├── .gitignore                      # Arquivos ignorados pelo Git
├── mvnw                            # Maven Wrapper (Linux/macOS)
├── mvnw.cmd                        # Maven Wrapper (Windows)
└── pom.xml                         # Dependências e configurações do Maven
```
