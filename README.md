<p align="center">
  <img src="auto-web-generator-frontend/src/assets/logo.svg" width="140" alt="Auto Web Generator logo">
</p>

<h1 align="center">Auto Web Generator</h1>
<p align="center"><i>AI-driven website generation, iteration, preview, and deploy</i></p>
<h2 align="center">⚠️ Project Status: In Progress</h2>
<h3 align="center">This project is not feature-complete yet. More improvements and new features are planned.</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Backend-Spring%20Boot%204-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/Frontend-Vue%203-42b883?style=flat-square&logo=vuedotjs&logoColor=white" alt="Vue 3">
  <img src="https://img.shields.io/badge/Language-TypeScript-3178c6?style=flat-square&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Database-PostgreSQL-336791?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Session-Redis-DC382D?style=flat-square&logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/AI-LangChain4j-black?style=flat-square" alt="LangChain4j">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white">
  <img src="https://img.shields.io/badge/UI-Ant%20Design%20Vue-0170FE?style=for-the-badge&logo=antdesign&logoColor=white">
  <img src="https://img.shields.io/badge/State-Pinia-FFD859?style=for-the-badge&logo=pinia&logoColor=black">
  <img src="https://img.shields.io/badge/Runtime-Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
</p>

---

<h2 align="center">About The Project</h2>

Auto Web Generator is a full-stack platform for turning prompts into web apps. It supports user registration and login, per-user project ownership, prompt-to-code generation, iterative chat-based updates, live in-app preview, project download as ZIP, and a unique deploy link for each published app.

This project uses GPT-5-mini for AI code generation workflows.

> ⚠️ **Note:** Code generation speed may vary depending on prompt complexity. Live preview and deployment behavior also depend on your runtime setup and timeout limits (for example, proxy, container, or platform request timeouts).

Core implementation:
- Backend API and app lifecycle: [src/main/java/com/example/autowebgenerator/controller/AppController.java](src/main/java/com/example/autowebgenerator/controller/AppController.java)
- AI orchestration: [src/main/java/com/example/autowebgenerator/core/AiCodeGeneratorFacade.java](src/main/java/com/example/autowebgenerator/core/AiCodeGeneratorFacade.java)
- Chat + preview workspace: [auto-web-generator-frontend/src/pages/app/AppChatPage.vue](auto-web-generator-frontend/src/pages/app/AppChatPage.vue)
- Visual DOM-targeted editor: [auto-web-generator-frontend/src/utils/visualEditor.ts](auto-web-generator-frontend/src/utils/visualEditor.ts)

---

<h2 align="center">Feature Overview</h2>

| Area | Details |
|:--|:--|
| End-to-end generation | Full-stack AI web builder (Spring Boot + Vue) that converts user requirements into deployable Vue apps through an agent workflow. |
| AI orchestration | OpenAI integration through LangChain4j to produce structured, consistent generation responses from natural-language prompts. |
| Real-time editing | Chat-driven editor (Vue + TypeScript) with Spring Boot SSE streaming for live AI output and agent logs, synced to an iframe preview via `postMessage`. |
| Stateful multi-turn sessions | Redis-backed chat memory and isolated user sessions to preserve context per app/user and prevent cross-session mixing. |
| Automated cover screenshots | Selenium-based headless browser capture generates app cover images for deployed projects. |

---

<h2 align="center">Tech Stack</h2>

<div align="center">

### Core
<a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"></a>
<a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Vue_3-42B883?style=for-the-badge&logo=vuedotjs&logoColor=white" alt="Vue 3"></a>
<a href="https://vite.dev/"><img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite"></a>
<a href="https://www.typescriptlang.org/"><img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript"></a>

### AI and Data
<a href="https://github.com/langchain4j/langchain4j"><img src="https://img.shields.io/badge/LangChain4j-000000?style=for-the-badge" alt="LangChain4j"></a>
<a href="https://www.postgresql.org/"><img src="https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"></a>
<a href="https://redis.io/"><img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis"></a>
<a href="https://platform.openai.com/docs/overview"><img src="https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white" alt="OpenAI"></a>

### UI and Runtime
<a href="https://antdv.com/"><img src="https://img.shields.io/badge/Ant_Design_Vue-0170FE?style=for-the-badge&logo=antdesign&logoColor=white" alt="Ant Design Vue"></a>
<a href="https://pinia.vuejs.org/"><img src="https://img.shields.io/badge/Pinia-FFD859?style=for-the-badge&logo=pinia&logoColor=black" alt="Pinia"></a>
<a href="https://www.docker.com/"><img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"></a>
<a href="https://nginx.org/"><img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white" alt="Nginx"></a>

</div>

---

<h2 align="center">Installation (Docker)</h2>

<details open>
<summary><b>1) Clone Repository</b></summary>

```bash
git clone https://github.com/<your-org>/auto-web-generator.git
cd auto-web-generator
```

</details>

<details open>
<summary><b>2) Configure Environment</b></summary>

Create a `.env` file in the root (or copy from `.env.example`) and set:

```env
DB_URL=jdbc:postgresql://<postgres-host>:5432/auto_web_generator?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=change-me

OPENAI_API_KEY=change-me
OPENAI_MODEL=gpt-4.1
OPENAI_REASONING_MODEL=gpt-5-mini

VITE_API_BASE=http://localhost:8123/api
VITE_DEPLOY_DOMAIN=http://localhost
```

</details>

<details open>
<summary><b>3) Start Containers</b></summary>

```bash
docker compose up --build -d
```

Optional (with nginx):

```bash
docker compose --profile nginx up --build -d
```

</details>

<details open>
<summary><b>4) Initialize Database</b></summary>

Run these SQL scripts in your PostgreSQL database:

```text
src/main/resources/sql/create_table.sql
src/main/resources/db/chat_history.sql
```

</details>

<details open>
<summary><b>5) Access Services</b></summary>

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8123/api`
- Health check: `http://localhost:8123/api/health/`

</details>

---

<h2 align="center">Project Architecture</h2>

<details>
<summary><b>Folder Structure</b></summary>

```text
.
|-- README.md
|-- pom.xml
|-- docker-compose.yaml
|-- nginx.conf
|-- mvnw
|-- mvnw.cmd
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   `-- com/example/autowebgenerator/
|   |   |       |-- ai/
|   |   |       |-- annotation/
|   |   |       |-- aop/
|   |   |       |-- common/
|   |   |       |-- config/
|   |   |       |-- constant/
|   |   |       |-- controller/
|   |   |       |-- core/
|   |   |       |-- exception/
|   |   |       |-- mapper/
|   |   |       |-- model/
|   |   |       |-- service/
|   |   |       `-- utils/
|   |   `-- resources/
|   |       |-- db/
|   |       |-- prompt/
|   |       `-- sql/
|   `-- test/
|       `-- java/
|-- auto-web-generator-frontend/
|   |-- public/
|   `-- src/
|       |-- access/
|       |-- api/
|       |-- assets/
|       |-- components/
|       |-- layouts/
|       |-- pages/
|       |   |-- admin/
|       |   |-- app/
|       |   `-- user/
|       |-- router/
|       |-- stores/
|       `-- utils/
|-- deploy/
|   `-- docker/
|       |-- backend/
|       |-- frontend/
|       `-- nginx/
|-- tmp/
|   |-- code_output/
|   |-- code_deploy/
|   `-- covers/
`-- .github/
```

</details>

Directory notes:
- [src/main/resources/prompt](src/main/resources/prompt): system prompts for generation modes
- [src/main/java/com/example/autowebgenerator/ai/tool](src/main/java/com/example/autowebgenerator/ai/tool): tools exposed to the AI agent
- [auto-web-generator-frontend/src/pages/app](auto-web-generator-frontend/src/pages/app): chat and preview workflow
- [deploy/docker](deploy/docker): container build definitions
- [tmp](tmp): generated output, deployed assets, and cover images

---

<h2 align="center">License</h2>

This project is licensed under the [MIT License](LICENSE).
