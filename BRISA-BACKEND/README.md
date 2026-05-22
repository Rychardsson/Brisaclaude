# BRISA — Backend

API REST construída com **Spring Boot 3.1.1** e **Java 21**, responsável por autenticação JWT, operações CRUD, importações via Excel, logs de auditoria e módulos analíticos.

---

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21** ou superior — [Download JDK](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** — [Download Maven](https://maven.apache.org/download.cgi)
- **PostgreSQL 12+** — [Download PostgreSQL](https://www.postgresql.org/download/)
- **Git** — [Download Git](https://git-scm.com/downloads)

---

## Configuração do banco de dados

1. Instale e inicie o PostgreSQL.
2. Crie o banco de dados `brisa`:

```sql
CREATE DATABASE brisa;
```

3. Configure as credenciais no arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brisa
spring.datasource.username=postgres
spring.datasource.password=sua-senha
```

> **Nota:** altere `username` e `password` conforme as configurações locais do seu PostgreSQL.

---

## Configuração de e-mail (opcional)

Para habilitar o envio de e-mails (verificação de conta, recuperação de senha), configure no `application.properties`:

```properties
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-de-app
spring.mail.from=seu-email@gmail.com
```

> **Importante:** utilize uma senha de aplicativo do Gmail — não a senha da conta.

---

## Como rodar o projeto

### Opção 1: Docker Compose (recomendada)

Sobe o backend, o frontend e o banco de dados juntos. Execute a partir da **raiz do repositório**:

```powershell
docker compose up -d
```

O backend ficará disponível em: `http://localhost:8082`

### Opção 2: Maven Wrapper

**Windows (PowerShell):**

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Opção 3: Maven instalado globalmente

```bash
mvn clean install
mvn spring-boot:run
```

### Opção 4: executar o JAR diretamente

```bash
mvn clean package
java -jar target/brisa-0.0.1-SNAPSHOT.jar
```

Após iniciar, o servidor estará disponível em: `http://localhost:8082`

---

## Variáveis de ambiente

Todas as configurações sensíveis podem ser sobrescritas via variáveis de ambiente:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SERVER_PORT` | `8082` | Porta HTTP do servidor |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/brisa` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | — | Senha do banco |
| `JWT_SECRET` | `my-secret-key` | Chave de assinatura JWT |
| `SPRING_MAIL_USERNAME` | — | Conta de e-mail |
| `SPRING_MAIL_PASSWORD` | — | Senha de app do Gmail |
| `CAREER_AUTOMATION_CRON` | `0 0 */6 * * *` | Cron de automação de carreiras |

Exemplo de uso:

```bash
export JWT_SECRET=minha-chave-super-secreta
java -jar target/brisa-0.0.1-SNAPSHOT.jar
```

---

## Tecnologias utilizadas

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Spring Boot | 3.1.1 | Framework principal |
| Spring Data JPA | — | Persistência de dados |
| Spring Security | — | Autenticação e autorização |
| PostgreSQL | 12+ | Banco de dados principal |
| Lombok | 1.18.44 | Redução de código boilerplate |
| java-jwt (Auth0) | 4.4.0 | Geração e validação de tokens JWT |
| Spring Mail | — | Envio de e-mails |
| Apache POI | 5.2.5 | Leitura de arquivos Excel |
| Maven | 3.8+ | Gerenciamento de dependências |

---

## Estrutura do projeto

```text
src/main/java/com/example/brisa/
├── config/        # Configurações (CORS, segurança, etc.)
├── controllers/   # Controladores REST
├── dtos/          # Data Transfer Objects
├── enums/         # Enumerações
├── exceptions/    # Tratamento de exceções
├── infra/         # Infraestrutura (filtros, segurança)
├── models/        # Entidades JPA
├── repositories/  # Repositórios JPA
├── services/      # Lógica de negócio
└── seeders/       # Dados iniciais (seed)
```

---

## Solução de problemas

### Erro de conexão com o PostgreSQL

- Verifique se o PostgreSQL está em execução.
- Confirme as credenciais em `application.properties`.
- Certifique-se de que o banco `brisa` foi criado.

### Porta já em uso

Altere a porta no `application.properties`:

```properties
server.port=8083
```

### Erro de compilação do Lombok

- Certifique-se de que o suporte ao Lombok está habilitado na sua IDE.
- Execute: `mvn clean install -U`

---

## Notas adicionais

- O projeto usa **Hibernate DDL `auto-update`**, que cria e atualiza as tabelas automaticamente no banco.
- A porta padrão é **8082** e pode ser alterada via `server.port` ou pela variável `SERVER_PORT`.

---

## Licença

Este projeto está sob a licença [MIT](../LICENSE).
