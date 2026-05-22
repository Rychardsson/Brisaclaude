# Brisa-ONE

Workspace principal do projeto BRISA, organizado em tres frentes:

- `BRISA-FRONTEND`: aplicacao Vue 3 + Vite que consome a API oficial (stack oficial).
- `BRISA-BACKEND`: API Spring Boot responsavel por autenticacao, cadastros, imports, logs e modulos analiticos (stack oficial).
- `Brisa ONE`: bundle React + Vite derivado do Figma, usado como prototipo visual isolado.

## Estrutura do repositorio

```text
Brisa-ONE/
|-- BRISA-FRONTEND/
|-- BRISA-BACKEND/
`-- Brisa ONE/
```

## O que faz parte da stack oficial

A stack oficial em runtime e:

- `BRISA-FRONTEND`
- `BRISA-BACKEND`

O diretorio `Brisa ONE` nao participa da navegacao oficial do sistema. Ele serve como referencia visual e prototipo separado.

## Integracao atual

Hoje o frontend principal e o backend estao integrados nos fluxos abaixo:

- autenticacao JWT
- dashboard
- painel administrativo
- carreira
- usuarios
- cursos
- pessoas
- programas e turmas
- imports de:
  - pessoas
  - programas
  - turmas
  - matriculas
  - instituicoes

## Requisitos

- Java 21
- Node.js 18+ e npm
- PostgreSQL (local ou remoto)

## Subida local

### 1. Backend

Por padrao, o backend sobe com PostgreSQL em:

- banco: `jdbc:postgresql://localhost:5432/brisa`
- porta HTTP: `8082`

Subida:

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run
```

Se precisar sobrescrever configuracoes locais:

```powershell
$env:SERVER_PORT='8082'
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/brisa'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD='sua-senha'
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.postgresql.Driver'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO='update'
.\mvnw.cmd spring-boot:run
```

### 2. Frontend principal

```powershell
cd BRISA-FRONTEND
npm install
npm run dev
```

Arquivo de ambiente de exemplo:

```dotenv
VITE_API_BASE_URL=http://localhost:8082/api
```

O exemplo esta em [BRISA-FRONTEND/.env.example](BRISA-FRONTEND/.env.example).

### 3. Prototipo visual

Opcional:

```powershell
cd "Brisa ONE"
npm install
npm run dev
```

## Comandos uteis

### Backend

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd clean install
```

### Frontend principal

```powershell
cd BRISA-FRONTEND
npm run dev
npm run build
npm run preview
```


## Convencoes importantes

- A API oficial expoe endpoints em `/api/*`.
- O frontend principal centraliza chamadas em `src/services/api.js`.
- Rotas protegidas no frontend usam `meta.requiresAuth`.
- O backend usa logs de auditoria para operacoes de create, update, delete e import.

## Observacoes

- O repositorio principal agora se chama `Brisa-ONE`.
- O fork ainda pode continuar com outro nome sem impactar o funcionamento local.
- Se voce alterar porta ou URL da API, alinhe `VITE_API_BASE_URL` no frontend.
