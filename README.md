# Brisa-ONE

Workspace principal do sistema BRISA ONE, responsavel pela gestao academica dos programas de capacitacao da BRISA.

O sistema acompanha alunos desde a inscricao inicial ate o acompanhamento de carreira por ate dois anos apos a conclusao, integrando cadastro operacional, importacoes via planilha, acompanhamento de turmas, projetos, provas, dashboard e backoffice.

## Estrutura

```text
Brisa-ONE/
|-- BRISA-FRONTEND/     # Frontend oficial em Vue 3 + Vite
|-- BRISA-BACKEND/      # API oficial em Spring Boot
|-- docker-compose.yml  # Orquestracao local
`-- README.md
```

## Stack oficial

| Servico | Tecnologia | Porta padrao |
| --- | --- | --- |
| Frontend | Vue 3 + Vite + Nginx | `4300` |
| Backend | Spring Boot 3 + Java 21 | `8082` |
| Banco de dados | PostgreSQL 16 | `5432` |

## Escopo funcional coberto

- Programas, turmas, escolas/parceiros, cursos, etapas, pessoas, matriculas, usuarios e logs.
- Cadastro de programas com contrato de fomento, entidade de fomento, datas, valor, coordenador geral e dados complementares.
- Cadastro dedicado de orientadores e gestores com perfil academico e status.
- Aditivos de programa com numeracao sequencial e atualizacao da data final do programa.
- Cursos com carga horaria, area de conhecimento e avaliacoes vinculadas.
- Pessoas/alunos com dados academicos, cidade, genero, cota, formacao e instituicao de origem.
- Turmas com escola/parceiro, programa, periodo, vagas e estrutura de etapas.
- Importacao Excel para pessoas, inscritos/candidatos, aprovados, alunos da imersao, cursos da turma, progresso de cursos e prova.
- Desempenho de alunos por curso, frequencia, nota, status e conclusao.
- Provas com questoes, assuntos, respostas, resumo, perfil dos respondentes e ranking de aprovados.
- Grupos de projeto com alunos, orientador, tema, empresa parceira, repositorio e encontros.
- Progressao de carreira de egressos com acompanhamento ate 24 meses.
- Automacao real de e-mail da carreira com configuracao e agendamento.
- Dashboard, Painel de Administracao, Carreira, Programas, Pessoas, Cursos, Turmas, Matriculas e Instituicoes integrados ao backend.
- Relatorio de Status da Turma com quantidade de alunos por numero de cursos concluidos.

## Pontos modelados de forma integrada

- Orientadores e gestores possuem cadastro dedicado e continuam podendo ser relacionados aos fluxos academicos existentes.
- Contrato de fomento continua ancorado no cadastro e no detalhe de programa, com aditivos mantidos no mesmo contexto.
- Cursos por escola/parceiro sao controlados pelo vinculo dos cursos as turmas do programa.

## Requisitos nao funcionais

- Autenticacao JWT e rotas protegidas no frontend.
- Auditoria de criacao, edicao, exclusao e importacao via logs do sistema.
- Importacoes com validacao de arquivo e mensagens de erro para o usuario.
- Separacao entre frontend, backend e banco para facilitar deploy e manutencao.
- Configuracao por variaveis de ambiente para banco, JWT, e-mail e URL da API.
- UI responsiva e consistente entre dashboard, backoffice, carreira e telas academicas.

## Criterios de aceitacao

- O administrador consegue autenticar, navegar pelo menu principal e acessar dashboard, painel, carreira, programas, pessoas e turmas.
- O administrador consegue cadastrar e editar programas, turmas, cursos, pessoas, usuarios e instituicoes conforme permissao.
- O administrador consegue importar planilhas nos fluxos existentes e receber retorno de sucesso, conflito ou erro.
- A turma exibe pessoas vinculadas, etapas, cursos, status de conclusao, prova, candidatos e grupos de projeto.
- A prova gera resumo, distribuicao de notas, melhores/piores questoes, perfil dos respondentes e relacao de aprovados.
- A carreira exibe egressos, janelas de acompanhamento, status profissional e configuracao de e-mails recorrentes.
- O sistema compila com `npm run build` no frontend e `.\mvnw.cmd test` no backend.

## Rodando com Docker

```powershell
Copy-Item .env.example .env
docker compose up -d
```

| Servico | URL |
| --- | --- |
| Frontend | <http://localhost:4300> |
| Backend | <http://localhost:8082> |

## Rodando localmente

Backend:

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd BRISA-FRONTEND
npm install
npm run dev
```

Configure a URL da API no frontend:

```dotenv
VITE_API_BASE_URL=http://localhost:8082/api
```

## Verificacao

```powershell
cd BRISA-BACKEND
.\mvnw.cmd test

cd ..\BRISA-FRONTEND
npm run build
```

## Observacoes

- A API oficial expoe endpoints em `/api/*`.
- O frontend centraliza chamadas HTTP em `BRISA-FRONTEND/src/services/api.js`.
- Senhas, JWT secret e credenciais de e-mail devem ficar somente em `.env`.
- O diretorio `BRISA-FRONTEND/src/components/figma-generated` serve como referencia visual e componentes auxiliares, mas a aplicacao oficial esta nas views Vue do frontend principal.
