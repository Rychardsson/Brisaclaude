# Checklist do Sistema BRISA One

Revisao: 2026-05-25

Base: documento de requisitos informado pelo usuario, decisao de manter contrato dentro de Programas, planilhas reais em `planilhas/` e validacoes de build desta rodada.

Legenda:

- OK: existente, ajustado ou validado no codigo.
- PARCIAL: existe base funcional, mas ainda falta completar fluxo, importar formato especifico ou homologar visualmente de ponta a ponta.
- N/A: fora do escopo solicitado.

## 1. Resumo executivo

| Area | Status | Observacao |
| --- | --- | --- |
| Gestao academica de programas BRISA | OK | O sistema cobre programas, turmas, escolas/parceiros, pessoas, etapas, cursos, avaliacoes, desempenho, projetos e carreira. |
| Contrato e aditivos | OK | Contrato permanece dentro de Programas. Aditivos ficam vinculados ao programa, numeram automaticamente quando nao informado e atualizam a data final. |
| Orientador no lugar de professor | OK | Interface e regras usam Orientador/Gestor; o enum legado `PROFESSOR` fica apenas para normalizar dados antigos. |
| Automacao de carreira com link/token | OK | Disparo gera token valido por 1 dia, e-mail inclui link publico, egresso valida email + token e envia formulario de carreira. |
| Planilhas reais principais | OK | Candidatos, aprovados, prova de nivelamento e relatorios semanais de cursos foram contemplados. |
| Planilha de imersao completa | PARCIAL | Estruturas de projetos/orientadores existem, mas a importacao automatica de bancas, pares, competencias e projetos da planilha de imersao ainda nao foi fechada. |

## 2. Entidades e relacionamentos

| Relacionamento exigido | Status | Observacao |
| --- | --- | --- |
| Programa -> Turma | OK | Turmas vinculadas ao programa. |
| Programa -> Turma -> Parceiro/Escola | OK | Escolas/parceiros e turmas existem no dominio. |
| Programa -> Contrato -> Aditivo | OK | Contrato fica como campos do programa, aparece tambem na visao da turma; aditivos ficam vinculados ao programa. |
| Programa -> Turma -> Parceiro/Escola -> Etapa -> Curso -> Avaliacoes ate 4 | OK | Etapas, cursos e avaliacoes existem; backend limita curso a no maximo 4 avaliacoes. |
| Programa -> Turma -> Parceiro/Escola -> Aluno -> Curso terminado | OK | Desempenho por curso e conclusao sao importados e consultados. |
| Programa -> Turma -> Parceiro/Escola -> Projeto -> Aluno | PARCIAL | Cadastro/manual de grupos existe; importacao automatica da planilha de imersao segue pendente. |
| Programa -> Turma -> Parceiro/Escola -> Orientador/Gestor | OK | Cadastro academico e associacao de orientador/gestor existem. |

## 3. Cadastros

| Cadastro | Status | Observacao |
| --- | --- | --- |
| Programas | OK | Nome, codigo, contrato/fomento, entidade, datas, valor e coordenacao ficam no programa. |
| Aditivos | OK | Numero sequencial automatico quando omitido; impede duplicidade no mesmo programa. |
| Entidade parceira/escola | OK | Instituicoes/parceiros vinculaveis ao programa e turma. |
| Cursos | OK | Cadastro e importacao por Excel, incluindo area/subarea/carga horaria. |
| Avaliacoes do curso | OK | CRUD por curso com limite de 4 avaliacoes. |
| Orientadores/Gestores | OK | Cadastro possui CPF, nome, formacao, nascimento, e-mail, tipo e ativo/inativo. |
| Turmas | OK | Cadastro contempla codigo, escola/parceiro, programa, datas e vagas. |
| Alunos via Excel | OK | Importacoes aceitam CPF/nome/e-mail e enriquecem dados quando a planilha traz campos adicionais. |
| Matricula aluno/turma | OK | Vinculo e status existem via matriculas e candidatos de etapa. |
| Desempenho do aluno | OK | Importacao LMS/relatorios semanais registra nota, progresso, data/status e curso. |
| Prova | PARCIAL | Resultado por aluno e questoes foi contemplado; cadastro explicito de assunto/cod_assunto por questao ainda precisa fechar se for obrigatorio. |
| Grupo de projeto | PARCIAL | Cadastro com tema, empresa/parceira, link de repositorio, orientador e alunos existe; falta importacao automatica da planilha de imersao. |
| Progressao de carreira | OK | Historico com data, status, empresa, cargo e notas; agora tambem recebe resposta publica por token. |

## 4. Funcionalidades

| Funcionalidade | Status | Observacao |
| --- | --- | --- |
| Cadastrar programa | OK | Fluxo existente. |
| Incluir aditivo ao programa | OK | Aditivo altera data final do programa pela maior data final cadastrada. |
| Cadastrar escola/parceiro | OK | Fluxo existente. |
| Cadastrar/carregar cursos | OK | Cadastro e importacao ajustados para orientador. |
| Cadastrar/importar alunos | OK | Planilhas reais de candidatos e aprovados foram consideradas. |
| Cadastrar turma | OK | Turma vinculada a programa e escola/parceiro. |
| Incluir cursos previstos na turma | OK | Vinculo curso/turma existe. |
| Associar orientador aos cursos da turma | OK | CourseAssignment possui orientador e a importacao preenche quando disponivel. |
| Clonar turma existente | OK | Clonagem agora preserva cursos, obrigatoriedade e orientadores associados. |
| Importar alunos da turma | OK | Importacao e vinculo por etapa/turma existem. |
| Incluir aluno manualmente | OK | UI e backend permitem criar/vincular pessoa manualmente em turma/etapa. |
| Inativar aluno manualmente | OK | Matricula possui status editavel; calculos de desempenho passam a considerar somente matriculas ativas. |
| Carregar desempenho de alunos | OK | Relatorios semanais em varias abas sao suportados. |
| Carregar resultado da prova | OK | Importacao de prova de nivelamento com nota total e questoes foi contemplada. |
| Automacao de carreira | OK | Configura checkpoints, envia e-mail, gera token e registra resposta do egresso. |

## 5. Relatorios e consultas

| Relatorio/Consulta | Status | Observacao |
| --- | --- | --- |
| Status da Turma em tabela/grafico | OK | Considera apenas alunos ativos nos calculos de desempenho. |
| Alunos por quantidade de cursos concluidos | OK | Buckets de conclusao por cursos contemplados. |
| Resumo da Prova por sexo e cota | OK | Indicadores contemplados. |
| Resumo da Prova: nota zero e maior nota | OK | Indicadores contemplados. |
| Resumo da Prova: distribuicao por nota | OK | Indicador contemplado. |
| 15 questoes com mais acertos | OK | Estrutura contemplada. |
| 15 questoes com menos acertos | OK | Estrutura contemplada. |
| Perfil dos respondentes por curso de origem | OK | Estrutura contemplada. |
| Perfil: cursos/cotas/cidades por acertos | OK | Estrutura contemplada. |
| Relacao de aprovados parametrizavel | OK | Ranking considera parametros, cotas e desempate por idade quando houver data de nascimento. |
| Evolucao dos egressos | OK | Carreira acompanha empregados/desempregados, cargo, empresa e historico. |

## 6. Planilhas reais

| Arquivo | Status | Observacao |
| --- | --- | --- |
| `Candidatos - com emails.xlsx` | OK | Importa candidatos/alunos por nome e e-mail. |
| `Aprovados Imersao UFG.xlsx` | OK | Atualiza aprovacao/classificacao e cota. |
| `PN - UFG 2025.2-Prova de Nivelamento 2025.2-notas corrigidas.xlsx` | OK | Importa e-mail, nota total e questoes do formato Moodle. |
| `RelatorioSemanalUFG(2/3/4).xlsx` | OK | Importa desempenho por abas de curso e ignora aba consolidada quando necessario. |
| `Planlhas de avaliacao de Imersao Turma 1 Goiania.xlsx` | PARCIAL | Estrutura analisada; falta importador especifico para bancas, competencias, pares, projetos e orientadores a partir desse arquivo. |

## 7. UI/UX e consistencia

| Item | Status | Observacao |
| --- | --- | --- |
| Abas de Programa | OK | Visao Geral, Pessoas, Processo Seletivo e Etapas estao coerentes. |
| Aba Carreira/Automacao | OK | Textarea com altura controlada, historico com rolagem e fluxo publico de egresso com token de 1 dia. |
| Tela publica de carreira | OK | Rota `/carreira/acompanhamento` valida email + token e abre formulario sem navbar. |
| Termos de interface | OK | Interface principal usa Orientador/Gestor. |
| Cursos/avaliacoes da turma | OK | Rota real de cursos da turma esta acessivel. |
| Visao geral da turma | OK | Ciclo, contrato, cronograma, distribuicoes e ultimas atualizacoes agora sao calculados a partir de dados reais/mockados. |
| Cards/listas longas em Programas/Turmas | OK | Listas de etapas, cursos, grupos de imersao, distribuicoes e atualizacoes possuem altura limitada e rolagem interna. |
| Responsividade visual completa | PARCIAL | Builds passaram e as areas criticas receberam rolagem; ainda falta homologacao manual completa em mobile com massa real grande. |

## 8. Requisitos nao funcionais

| Categoria | Status | Criterio de aceite |
| --- | --- | --- |
| Desempenho | PARCIAL | Importacoes grandes devem concluir sem travar UI e retornar erros por linha. Builds passaram, mas falta teste de carga real. |
| Seguranca | OK | Rotas autenticadas protegidas por JWT; formulario publico exige email + token, expira em 1 dia e gera novo token em reenvios. |
| Usabilidade | OK | Fluxos principais tem UI para cadastro, importacao, consulta e acompanhamento. |
| Confiabilidade | PARCIAL | Importacoes principais tratam duplicidades/atualizacoes; falta suite automatizada cobrindo todos os formatos reais. |
| Auditoria/Historico | OK | Acoes relevantes geram logs; resposta publica de carreira tambem registra evento. |
| Restricoes/dependencias | PARCIAL | Formatos reais conhecidos; falta documentar formalmente cada layout aceito e colunas obrigatorias. |

## 9. Criterios de aceitacao

| Fluxo | Status | Criterio |
| --- | --- | --- |
| Criar programa completo | OK | Programa salva dados basicos, contrato interno, escola/parceiro, turma e etapas. |
| Incluir aditivo | OK | Aditivo recebe numero sequencial e atualiza a data final do programa. |
| Importar candidatos | OK | Planilha real cria/atualiza pessoas sem depender de CPF quando houver nome/e-mail. |
| Importar aprovados | OK | Atualiza aprovacao, classificacao e cota. |
| Importar prova | OK | Nota total e questoes aparecem para relatorios. |
| Importar desempenho LMS | OK | Cursos/progresso/notas entram na turma correta. |
| Gerar ranking de aprovados | OK | Parametros, cotas e desempate por idade contemplados. |
| Montar grupo de projeto manualmente | OK | Grupo recebe alunos, tema, empresa/parceira, repositorio e orientador. |
| Importar grupo/projeto da planilha de imersao | PARCIAL | Pendente importador especifico. |
| Acompanhar egresso manualmente | OK | Historico salva status, empresa, cargo e data. |
| Acompanhar egresso por link publico | OK | E-mail de automacao leva token valido por 1 dia; egresso valida e responde formulario. |

## 10. Pendencias restantes

1. Criar importador especifico para `Planlhas de avaliacao de Imersao Turma 1 Goiania.xlsx`, cobrindo bancas, competencias, pares, notas finais, projetos, orientadores e alunos.
2. Confirmar se `cod_assunto` por questao de prova precisa de cadastro proprio; hoje o sistema cobre resultado/questoes, mas nao uma entidade formal de assunto da questao.
3. Fazer homologacao visual completa em desktop/mobile de todas as abas com massa real grande.
4. Documentar layouts aceitos das planilhas e colunas obrigatorias/opcionais.
5. Homologar com usuarios reais os textos e indicadores da visao geral da turma antes da apresentacao final.

## 11. Validacoes executadas

| Validacao | Resultado |
| --- | --- |
| `mvnw.cmd -DskipTests package` | OK |
| `npm run build` | OK, com aviso padrao de chunk grande do Vite |
| Revisao de termos Orientador/Gestor | OK, interface principal alinhada; `PROFESSOR` permanece somente como compatibilidade legada no backend |
| `docker compose up -d --build frontend backend` | OK |
| API local da turma UFG 2025.2 | OK, contrato presente, 50 matriculas, 50 candidatos por etapa e 11 grupos de projeto |
