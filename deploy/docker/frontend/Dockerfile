FROM node:22-alpine AS build
WORKDIR /app

ARG VITE_API_BASE=/api
ARG VITE_DEPLOY_DOMAIN=http://localhost
ENV VITE_API_BASE=$VITE_API_BASE
ENV VITE_DEPLOY_DOMAIN=$VITE_DEPLOY_DOMAIN

COPY auto-web-generator-frontend/package*.json ./
RUN npm ci

COPY auto-web-generator-frontend/ ./
RUN npm run build-only

FROM nginx:1.29-alpine

ENV BACKEND_API_ORIGIN=http://backend:8123
COPY deploy/docker/frontend/nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 5173
