# BRISA — Frontend

Interface web construída com **Vue 3 + Vite**, que consome a API do BRISA-BACKEND. Inclui dashboard analítico, painel administrativo, acompanhamento de carreiras e fluxos de cadastro de programas.

---

## Pré-requisitos

- **Node.js** 20.19+ ou 22.12+
- **npm** 9+

---

## Configuração do ambiente

Crie o arquivo `.env` a partir do template:

```powershell
Copy-Item .env.example .env
```

Conteúdo do `.env`:

```dotenv
VITE_API_BASE_URL=http://localhost:8082/api
```

Ajuste a URL caso o backend esteja em outro endereço ou porta.

---

## Como rodar o projeto

### Opção 1: Docker Compose (recomendada)

Execute a partir da **raiz do repositório**. O frontend será compilado e servido pelo Nginx na porta `4300`:

```powershell
docker compose up -d
```

Acesse em: <http://localhost:4300>

### Opção 2: Servidor de desenvolvimento local

```powershell
npm install
npm run dev
```

O Vite iniciará o servidor com hot-reload. Acesse em: <http://localhost:5173>

---

## Scripts disponíveis

```powershell
npm run dev      # Servidor de desenvolvimento com hot-reload
npm run build    # Build de produção (saída em /dist)
npm run preview  # Pré-visualiza o build de produção localmente
```

---

## Tecnologias utilizadas

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| Vue 3 | ^3.5 | Framework principal |
| Vite | ^7.2 | Bundler e servidor de desenvolvimento |
| Vue Router | ^4.6 | Roteamento SPA |
| Axios | ^1.13 | Cliente HTTP |
| Chart.js | ^4.5 | Gráficos e dashboards |
| vue-chartjs | ^5.3 | Integração Chart.js com Vue |
| Headless UI | ^1.7 | Componentes acessíveis sem estilo |
| Lucide Vue Next | ^1.0 | Ícones |
| xlsx | ^0.18 | Leitura e exportação de Excel |

---

## Estrutura do projeto

```text
src/
├── assets/        # Fontes, imagens e estilos globais
├── components/    # Componentes reutilizáveis
├── router/        # Definição de rotas (index.js)
├── services/      # Camada de comunicação com a API
│   ├── api.js     # Instância Axios centralizada (com JWT)
│   ├── peopleService.js
│   ├── programService.js
│   └── ...
└── views/         # Páginas da aplicação
```

---

## Convenções

- **Todas** as chamadas HTTP devem usar a instância centralizada `src/services/api.js`, que injeta automaticamente o token JWT nos cabeçalhos.
- Rotas que exigem autenticação devem declarar `meta: { requiresAuth: true }` no `router/index.js`.
- O estado de autenticação (token e usuário) é persistido no `localStorage`.

---

## IDE recomendada

- [VS Code](https://code.visualstudio.com/) com a extensão [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (desabilite o Vetur se estiver instalado).

### Extensões de navegador para desenvolvimento

**Chromium (Chrome, Edge, Brave):**
- [Vue.js Devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)

**Firefox:**
- [Vue.js Devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)

---

## Referências

- [Documentação do Vite](https://vite.dev/config/)
- [Documentação do Vue 3](https://vuejs.org/)
- [Vue Router](https://router.vuejs.org/)
