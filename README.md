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
  - [Spring Boot 3.2.5](https://spring.io/projects/spring-boot)
  - [Spring Web](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
  - [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
  - [Spring Security](https://spring.io/projects/spring-security)
  - [Maven](https://maven.apache.org/)
- **Frontend:**
  - [Thymeleaf](https://www.thymeleaf.org/)
  - HTML / CSS / JavaScript
- **Banco de Dados & Infraestrutura:**
  - [MySQL](https://www.mysql.com/)
  - [Docker & Docker Compose](https://www.docker.com/)

## 🚀 Como Executar o Projeto

Existem duas formas de rodar o projeto: usando o **Docker** (recomendado, mais fácil) ou configurando **Localmente**.

### Opção 1: Usando Docker (Recomendado)

A maneira mais rápida de subir o banco de dados e a aplicação juntos é utilizando o Docker Compose.

**Pré-requisitos:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução.

1. Clone o repositório:
   ```bash
   git clone https://github.com/ryannnnnnnnnns/Confeitaria.git
   cd Confeitaria
   ```

2. Suba os containers da aplicação e do banco de dados MySQL:
   ```bash
   docker-compose up --build -d
   ```

3. Acesse a aplicação no navegador:
   [http://localhost:8080](http://localhost:8080)

*(Para parar a aplicação e o banco, rode `docker-compose down`)*

---

### Opção 2: Rodando Localmente (Sem Docker)

Siga os passos abaixo caso queira rodar a aplicação diretamente na sua máquina com a sua própria instalação do MySQL.

**Pré-requisitos:**
- [Java JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) ou superior.
- [Apache Maven](https://maven.apache.org/download.cgi) instalado e configurado.
- [MySQL Server](https://dev.mysql.com/downloads/mysql/) instalado e em execução.

1. **Clone o Repositório e acesse a pasta:**
   ```bash
   git clone https://github.com/ryannnnnnnnnns/Confeitaria.git
   cd Confeitaria
   ```

2. **Configure o Banco de Dados:**
   - Crie um banco de dados no seu MySQL com o nome `projeto`.
     ```sql
     CREATE DATABASE projeto;
     ```
   - O projeto está configurado para ler propriedades de ambiente, mas você pode editar o arquivo `src/main/resources/application.properties` ou exportar as variáveis de ambiente com seu usuário e senha do MySQL local.
   *(Nota: O esquema do banco será gerado automaticamente graças à propriedade `spring.jpa.hibernate.ddl-auto=update`)*

3. **Compile o Projeto:**
   **No Windows:**
   ```bash
   mvnw.cmd clean install
   ```
   **No Linux ou macOS:**
   ```bash
   ./mvnw clean install
   ```

4. **Execute a Aplicação:**
   **No Windows:**
   ```bash
   mvnw.cmd spring-boot:run
   ```
   **No Linux ou macOS:**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Acesse a Aplicação:**
   Após a inicialização, abra seu navegador e acesse: [http://localhost:8080](http://localhost:8080)

## 📂 Estrutura do Projeto

```
.
├── src
│   ├── main
│   │   ├── java/com/ifsp/projeto   # Código fonte da aplicação
│   │   │   ├── config              # Configurações de segurança e afins
│   │   │   ├── controller          # Controladores Spring MVC
│   │   │   ├── model               # Entidades JPA
│   │   │   ├── repository          # Repositórios Spring Data
│   │   │   └── service             # Lógica de negócio
│   │   └── resources
│   │       ├── static              # Arquivos estáticos (CSS, JS, imagens)
│   │       ├── templates           # Templates Thymeleaf (HTML)
│   │       └── application.properties # Configurações da aplicação
│   └── test                        # Testes da aplicação
├── docker-compose.yml              # Orquestração do Docker (App + DB)
├── Dockerfile                      # Receita de imagem Docker para o Spring Boot
├── .gitignore                      # Arquivos ignorados pelo Git
├── mvnw                            # Maven Wrapper (Linux/macOS)
├── mvnw.cmd                        # Maven Wrapper (Windows)
└── pom.xml                         # Dependências e configurações do Maven
```