# Brisa Claude

Este repositorio esta organizado em tres pastas principais:

- `BRISA-FRONTEND`: aplicacao Vue que consome a API oficial do projeto.
- `BRISA-BACKEND`: API Spring Boot responsavel por autenticacao, cadastros, importacoes e analytics.
- `Brisa ONE`: bundle React derivado do Figma, usado como referencia visual e prototipo isolado.

## Como as partes se conectam

- O frontend principal usa `VITE_API_BASE_URL` para falar com o backend.
- O backend expoe seus endpoints em `/api/*`.
- O prototipo `Brisa ONE` nao faz parte da stack oficial em runtime; ele serve como referencia de interface.

## Subida local recomendada

### 1. Backend

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run
```

Se voce quiser usar PostgreSQL, sobrescreva as variaveis:

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/brisa'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD='sua-senha'
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.postgresql.Driver'
.\mvnw.cmd spring-boot:run
```

### 2. Frontend principal

```powershell
cd BRISA-FRONTEND
npm install
npm run dev
```

Opcionalmente, copie `.env.example` para `.env` e ajuste a API:

```dotenv
VITE_API_BASE_URL=http://localhost:8082/api
```

### 3. Prototipo Brisa ONE

```powershell
cd "Brisa ONE"
npm install
npm run dev
```

## Usuarios seed

Quando o banco estiver vazio, o backend cria:

- `admin` / `admin12345`
- `user` / `user12345`

## Observacoes

- `Dashboard`, `Painel` e `Carreira` pertencem ao `BRISA-FRONTEND`.
- O backend agora mantem o padrao `/api/users`, alinhado ao consumo do frontend.
- O servico de logs do frontend foi corrigido para nao duplicar `/api`.
