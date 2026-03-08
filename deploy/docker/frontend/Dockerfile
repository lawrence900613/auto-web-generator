FROM node:22-alpine
WORKDIR /app

COPY auto-web-generator-frontend/package*.json ./
RUN npm ci

COPY auto-web-generator-frontend/ ./

EXPOSE 5173

CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]
