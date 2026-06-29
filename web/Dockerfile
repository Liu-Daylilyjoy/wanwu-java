FROM node:22-bookworm-slim AS build

WORKDIR /app/web

ENV HUSKY=0
ENV VUE_APP_BASE_PATH=

RUN corepack enable && corepack prepare pnpm@9.15.0 --activate

COPY web/package.json web/pnpm-lock.yaml web/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY web/ ./
RUN pnpm build

FROM nginx:1.27-alpine

COPY web/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/web/dist/ /usr/share/nginx/html/aibase/
