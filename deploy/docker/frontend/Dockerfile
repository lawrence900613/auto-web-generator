FROM node:22-alpine AS build
WORKDIR /app

COPY auto-web-generator-frontend/package*.json ./
RUN npm ci

COPY auto-web-generator-frontend/ ./
RUN npm run build-only

FROM nginx:1.29-alpine

COPY deploy/docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 5173
