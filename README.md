# Brisa-ONE

Workspace principal do projeto **BRISA**, organizado em três frentes:

- `BRISA-FRONTEND`: aplicação Vue 3 + Vite que consome a API oficial (stack oficial).
- `BRISA-BACKEND`: API Spring Boot responsável por autenticação, cadastros, importações, logs e módulos analíticos (stack oficial).
- `Brisa ONE`: bundle React + Vite derivado do Figma, usado como protótipo visual isolado.

---

## Estrutura do repositório

```text
Brisa-ONE/
├── BRISA-FRONTEND/     # Frontend Vue 3 + Vite
├── BRISA-BACKEND/      # Backend Spring Boot
├── docker-compose.yml  # Orquestração Docker
├── .env.example        # Template de variáveis de ambiente
└── docs/               # Documentação e requisitos
```

---

## Stack oficial

A stack oficial em execução é composta por:

| Serviço | Tecnologia | Porta padrão |
|---------|-----------|--------------|
| Frontend | Vue 3 + Vite + Nginx | `4300` |
| Backend | Spring Boot 3 + Java 21 | `8082` |
| Banco de dados | PostgreSQL 16 | `5432` |

> O diretório `Brisa ONE` não participa da navegação oficial do sistema. Ele serve apenas como referência visual e protótipo separado.

---

## Funcionalidades integradas

O frontend principal e o backend estão integrados nos seguintes fluxos:

- Autenticação JWT
- Dashboard analítico
- Painel administrativo
- Carreiras e progressão
- Usuários
- Cursos
- Pessoas
- Programas e turmas
- Importações via Excel de:
  - Pessoas
  - Programas
  - Turmas
  - Matrículas
  - Instituições

---

## Rodando com Docker (recomendado)

A forma mais simples de subir o projeto completo é via Docker Compose.

### Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução.

### 1. Configure as variáveis de ambiente

```powershell
Copy-Item .env.example .env
```

Edite o arquivo `.env` e ajuste as senhas conforme necessário (especialmente `POSTGRES_PASSWORD` e `JWT_SECRET` em produção).

### 2. Suba todos os serviços

```powershell
docker compose up -d
```

O Docker irá:
1. Baixar e iniciar o **PostgreSQL 16**.
2. Compilar e iniciar o **backend** Spring Boot (aguarda o banco estar saudável).
3. Compilar e iniciar o **frontend** Vue 3 servido pelo Nginx.

### 3. Acesse a aplicação

| Serviço | URL |
|---------|-----|
| Frontend | <http://localhost:4300> |
| API (Backend) | <http://localhost:8082> |

### Comandos úteis do Docker

```powershell
# Acompanhar logs em tempo real
docker compose logs -f

# Ver logs de um serviço específico
docker compose logs -f backend

# Parar todos os containers (dados do banco preservados)
docker compose down

# Parar e apagar o volume do banco de dados
docker compose down -v

# Recompilar após mudanças no código
docker compose build backend   # ou: frontend
docker compose up -d
```

---

## Rodando localmente (sem Docker)

### Pré-requisitos

- Java 21+
- Node.js 20.19+ ou 22.12+ e npm
- PostgreSQL 12+ (local ou remoto)

### 1. Backend

Por padrão, o backend conecta ao PostgreSQL em:

- Banco: `jdbc:postgresql://localhost:5432/brisa`
- Porta HTTP: `8082`

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run
```

Para sobrescrever configurações via variáveis de ambiente:

```powershell
$env:SPRING_DATASOURCE_URL     = 'jdbc:postgresql://localhost:5432/brisa'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = 'sua-senha'
$env:JWT_SECRET                = 'sua-chave-secreta'
.\mvnw.cmd spring-boot:run
```

### 2. Frontend

```powershell
cd BRISA-FRONTEND
npm install
npm run dev
```

Crie o arquivo `.env` com a URL da API:

```dotenv
VITE_API_BASE_URL=http://localhost:8082/api
```

O template está disponível em [`BRISA-FRONTEND/.env.example`](BRISA-FRONTEND/.env.example).

---

## Comandos úteis

### Backend

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run   # Inicia o servidor
.\mvnw.cmd test              # Executa os testes
.\mvnw.cmd clean install     # Compila e instala no repositório local
```

### Frontend

```powershell
cd BRISA-FRONTEND
npm run dev      # Servidor de desenvolvimento com hot-reload
npm run build    # Build de produção
npm run preview  # Pré-visualiza o build de produção
```

---

## Convenções importantes

- A API oficial expõe todos os endpoints em `/api/*`.
- O frontend centraliza as chamadas HTTP em `src/services/api.js`.
- Rotas protegidas no frontend utilizam `meta.requiresAuth`.
- O backend registra logs de auditoria para operações de criação, atualização, exclusão e importação.
- Senhas e segredos nunca devem ser commitados — use sempre o arquivo `.env` (ignorado pelo Git).

---

## Observações

- Se você alterar a porta ou a URL da API, atualize `VITE_API_BASE_URL` no frontend.
- O fork do repositório pode continuar com outro nome sem impactar o funcionamento local.
- Em ambiente de produção, substitua os valores padrão de `JWT_SECRET` e `POSTGRES_PASSWORD` por valores seguros e únicos.
