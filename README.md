# 🏢 Lobby API - Gestão Logística para Condomínios

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

> **Lobby** é uma solução backend robusta para digitalizar a portaria de condomínios. Substitui o caderno de papel por uma API segura, auditável e com notificações em tempo real. Acesse em https://www.uselobby.com.br/

---

## 🎯 O Problema
A gestão de encomendas em portarias tradicionais é falha: cadernos de papel ilegíveis, extravios de pacotes e falta de comunicação com os moradores geram insegurança e atrito.

## 💡 A Solução
Uma API RESTful desenvolvida com **Arquitetura em Camadas (Service Layer)**, focada em segurança e performance. O sistema gerencia o ciclo de vida da encomenda, desde a chegada até a retirada, notificando o morador instantaneamente via e-mail.

---

## 🔥 Funcionalidades de Engenharia

### 🔐 Segurança (Security & JWT)
* **Autenticação Stateless:** Uso de JWT (JSON Web Tokens) com controle de sessão via `tokenVersion` (permite invalidar tokens em caso de roubo ou banimento).
* **Proteção contra Brute-Force:** O sistema detecta tentativas falhas de login e **bane temporariamente** o IP/Usuário após 5 erros.
* **RBAC (Role-Based Access Control):**
    * `BUSINESS`: Gestão total do condomínio.
    * `SYNDIC`: Gestão total de usuários e encomendas (Aprovar porteiros, banir usuários, ver encomendas, confirmar encomendas, etc).
    * `DOORMAN`: Registrar e entregar encomendas.
    * `RESIDENT`: Apenas visualização de suas encomendas.

### 📧 Notificações Ricas (JavaMailSender)
* **E-mails Transacionais:** Integração SMTP (Gmail/Brevo).
* **Templates HTML:** O morador recebe um e-mail visualmente formatado com o código de rastreio assim que a encomenda chega.

### 📦 Logística & Rastreio
* **Rastreio Híbrido:** Gera automaticamente um código interno único (UUID curto) se não houver etiqueta.
* **Ciclo de Vida:** `WAITING_PICKUP` -> `DELIVERED`.
* **Auditoria:** Registo exato da data/hora de retirada (`withdrawalDate`).

### 📚 Documentação Viva
* **Swagger UI (OpenAPI 3):** Documentação interativa gerada automaticamente.
* Acessível em: `/swagger-ui/index.html`

---

## 🛠️ Stack Tecnológica

* **Core:** Kotlin, Java 17, Spring Boot 3.
* **Dados:** Spring Data JPA, PostgreSQL.
* **Segurança:** Spring Security 6, BCrypt, JWT.
* **Infraestrutura:** Docker, Railway (Cloud), Gradle.
* **Ferramentas:** Mailtrap/Gmail SMTP, IntelliJ IDEA.

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
* Java 17+
* Docker (Opcional, mas recomendado)

### 1. Clone o repositório
```bash 
git clone https://github.com/GBLins14/Lobby-API.git
cd Lobby-API
```

### 2. Configure as Variáveis de Ambiente
Crie um arquivo .env na raiz do projeto (baseado no !.env.example) e configure as suas credenciais.

### 3. Rodando com Docker (Recomendado) 🐳
```bash
docker build -t lobby-api .
docker run -p 8080:8080 --env-file .env lobby-api
```

### 4. Rodando Localmente (Gradle)
```bash
./gradlew bootRun
```

🤝 Projeto desenvolvido como MVP para estudo avançado de arquitetura backend com Kotlin.

<div align="center"> <sub>Desenvolvido por <b>Gabriel Lins</b> 🚀</sub> </div>
