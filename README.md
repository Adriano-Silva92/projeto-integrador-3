# 🎓 Sistema Acadêmico

Sistema acadêmico desenvolvido com Java e Spring Boot para gerenciamento de alunos, turmas, avaliações, notas, frequência e usuários.

---

# 🚀 Tecnologias Utilizadas

## Back-end

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA
* Hibernate
* Maven

## Front-end

* HTML5
* CSS3
* JavaScript
* Thymeleaf
* Bootstrap

## Banco de Dados

* PostgreSQL

## Deploy e DevOps

* Docker
* Fly.io
* Git
* GitHub

---

# 📚 Funcionalidades

## 👨‍🎓 Alunos

* Cadastro de alunos
* Listagem de alunos
* Associação de alunos às turmas
* Dashboard individual do aluno

## 👨‍🏫 Professores

* Cadastro de professores
* Associação de professores às turmas

## 🏫 Turmas

* Cadastro de turmas
* Edição de turmas
* Exclusão de turmas
* Visualização detalhada

## 📝 Avaliações

* Cadastro de avaliações
* Controle por tipo de avaliação
* Associação de avaliações às turmas

## 📊 Notas

* Cadastro de notas
* Cálculo de médias
* Visualização por aluno

## 📅 Frequência

* Registro de presença
* Registro de faltas
* Controle de frequência dos alunos
* Percentual de frequência

## 🔐 Autenticação e Acesso

* Sistema de login
* Controle de acesso por perfil
* Interceptador de autenticação
* Sessão de usuário

## 📈 Dashboard

* Cards informativos
* Estatísticas acadêmicas
* Atualização dinâmica via JavaScript/AJAX

## ♿ Acessibilidade

* Recursos básicos de acessibilidade
* Scripts de apoio à navegação

---

# 🗂️ Estrutura do Projeto

```bash
src
 ├── main
 │   ├── java
 │   │   └── br/appLogin/appLogin
 │   │       ├── controller
 │   │       ├── model
 │   │       ├── repository
 │   │       ├── config
 │   │       └── AppLoginApplication.java
 │   │
 │   ├── resources
 │   │   ├── static
 │   │   │   ├── css
 │   │   │   ├── js
 │   │   │   └── img
 │   │   │
 │   │   ├── templates
 │   │   └── application.properties
 │
 └── test
```

---

# ⚙️ Como Executar o Projeto

## 1️⃣ Clonar o repositório

```bash
git clone https://github.com/Adriano-Silva92/projeto-integrador-3.git
```

---

## 2️⃣ Entrar na pasta do projeto

```bash
cd projeto-integrador-3
```

---

## 3️⃣ Configurar o banco de dados

Edite o arquivo:

```bash
src/main/resources/application.properties
```

Configure:

```properties
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```

---

## 4️⃣ Executar o projeto

### Pelo Maven

```bash
mvn spring-boot:run
```

### Ou pelo Eclipse / STS

Execute a classe:

```bash
AppLoginApplication.java
```

---

# 🐳 Executando com Docker

## Build da imagem

```bash
docker build -t sistema-academico .
```

## Executar container

```bash
docker run -p 8080:8080 sistema-academico
```

---

# 🌐 Deploy

O projeto está preparado para deploy utilizando:

* Fly.io
* Docker

Arquivo utilizado:

```bash
fly.toml
```

---

# 📸 Telas do Sistema

## Login

* Sistema de autenticação
* Controle de sessão

## Dashboard

* Estatísticas acadêmicas
* Cards dinâmicos
* Indicadores do sistema

## Gestão Acadêmica

* Turmas
* Alunos
* Notas
* Frequência
* Usuários

---

# 🔒 Segurança

* Controle de autenticação
* Restrição por perfil
* Interceptador de rotas
* Proteção de sessão

---

# 📌 Melhorias Futuras

* Upload de foto de perfil
* Integração com Cloudinary
* Relatórios em PDF
* Exportação Excel
* Notificações em tempo real
* API REST
* Integração com aplicativo mobile
* Melhorias no dashboard

---

# 👨‍💻 Desenvolvedor

Desenvolvido por Adriano Silva.

GitHub:

[https://github.com/Adriano-Silva92](https://github.com/Adriano-Silva92)

---

# 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos e educacionais.
