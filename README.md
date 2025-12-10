# 🏢 Lobby API

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Security](https://img.shields.io/badge/spring%20security-%236DB33F.svg?style=for-the-badge&logo=spring-security&logoColor=white)
![JPA](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)

> **Lobby** é uma solução de logística inteligente para condomínios, focada em substituir o caderno de papel da portaria por uma gestão digital, segura e rastreável.

## 🎯 O Problema
A gestão de encomendas em portarias é caótica. Cadernos de papel, falta de aviso aos moradores e dificuldade em localizar pacotes geram insegurança e atrasos.

## 💡 A Solução (MVP)
Uma API RESTful robusta que gerencia o ciclo de vida completo de uma encomenda, desde a chegada na portaria até a retirada pelo morador, com controle de acesso rigoroso.

---

## 🔥 Funcionalidades Principais

### 🔐 Segurança & Controle de Acesso (RBAC)
* **Autenticação JWT:** Login seguro com tokens expiráveis.
* **Perfis de Usuário:**
    * `ROLE_DOORMAN`: Acesso administrativo para registrar e entregar pacotes.
    * `ROLE_RESIDENT`: Acesso restrito para visualizar apenas suas próprias encomendas.
* **Fluxo de Aprovação:** Contas de porteiros são criadas com status `PENDING` e bloqueadas automaticamente até aprovação do administrador/síndico.

### 📦 Gestão Logística
* **Registro Inteligente:** O porteiro vincula a encomenda ao morador.
* **Auto-Tracking:** Se a encomenda não tiver código de rastreio, o sistema gera um identificador único (ex: `LOBBY-A1B2C3`) automaticamente.
* **Baixa Segura:** Confirmação de retirada com registro de data/hora (`withdrawalDate`) e mudança de status para `DELIVERED`.
* **Validação de Status:** O sistema impede que uma encomenda já entregue seja baixada novamente.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Kotlin ⚡
* **Framework:** Spring Boot 3
* **Segurança:** Spring Security + JWT Filters
* **Banco de Dados:** PostgreSQL (Produção) / H2 (Dev)
* **ORM:** Spring Data JPA (Hibernate)
* **Build Tool:** Gradle

---

## 🚀 Endpoints da API

### 🔑 Autenticação
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/auth/sign-up` | Cria nova conta (Porteiros nascem PENDING). |
| `POST` | `/auth/sign-in` | Login e geração de Token JWT. |

### 👮 Porteiro (Doorman)
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/doorman/deliveries` | Registra nova encomenda para um morador. |
| `GET` | `/api/doorman/deliveries/{code}` | Busca detalhes de uma encomenda pelo código. |
| `PUT` | `/api/doorman/deliveries/{code}/confirm` | Confirma a retirada (Muda status para DELIVERED). |

### 🏠 Morador (Resident)
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/deliveries` | Lista histórico de encomendas pessoais. |

---

## 🏃‍♂️ Como Rodar

1. Clone o repositório:
```bash
git clone [https://github.com/seu-usuario/lobby-api.git](https://github.com/seu-usuario/lobby-api.git)