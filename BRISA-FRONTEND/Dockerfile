# ─── Etapa 1: Build ────────────────────────────────────────────────────────
FROM node:22-alpine AS build

WORKDIR /app

# Copia arquivos de dependências antes do código (melhor cache)
COPY package*.json ./
RUN npm ci --frozen-lockfile

# Copia o restante do código-fonte
COPY . .

# Build de produção (VITE_API_BASE_URL pode ser injetada via ARG ou .env)
ARG VITE_API_BASE_URL=http://localhost:8082/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ─── Etapa 2: Servidor Nginx ───────────────────────────────────────────────
FROM nginx:1.27-alpine

# Remove config padrão do Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia nossa configuração customizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia os arquivos de build
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
