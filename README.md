# CarDekho AI Buyer Guide

A small full-stack MVP for the take-home: help a confused car buyer go from vague preferences to a confident shortlist.

## What I built

- A React app with:
  - a natural-language buyer brief input
  - a simple buyer quiz
  - AI-backed shortlist recommendations
  - AI-powered "Why this car?" explanations on each result card
  - a quick comparison view
  - saved shortlists
- A Spring Boot backend with:
  - a seeded car catalog loaded from `cars.json`
  - AI-assisted intent extraction from messy buyer text
  - weighted recommendation scoring logic with confidence scores
  - Gemini-powered summary generation with a graceful fallback
  - H2-backed shortlist persistence

## Why this scope

The highest-value user flow is:

1. let a confused buyer describe their need in plain language
2. turn that into structured preferences
3. reduce the catalog to 3 strong options
4. explain why those options fit
5. let the buyer save that shortlist

That is the core “I don’t know what to buy” to “I have a shortlist” journey, so I prioritized that over extra filters, auth, admin tools, or a bigger dataset.

## What I deliberately cut

- Login/auth
- Large real-world car ingestion pipeline
- Dealer/location inventory
- Complex compare charts
- End-to-end tests
- Deployment automation

## Tech stack and why

- Frontend: React + Vite + React Router
  - Fastest way to ship a clean multi-page UI.
- Backend: Spring Boot + Spring Web + Spring Data JPA + H2
  - Simple Java backend with enough structure for persistence and business logic.
- AI: Google Gemini API
  - Supports a free API key through Google AI Studio for MVP experimentation.

## AI key setup

Set an environment variable before starting the backend:

```powershell
$env:GEMINI_API_KEY="your_key_here"
```

If no key is provided, the app still works and falls back to a deterministic summary so the demo does not break.

## Run locally

### Option 1: One-command launcher

From the repo root:

```powershell
.\start-local.ps1
```

This opens two PowerShell windows:
- Spring Boot on `http://localhost:8080`
- React on `http://localhost:5173`

### Option 2: Manual

Backend:

```powershell
cd backend
mvn "-Dmaven.repo.local=.m2repo" spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

## Deploy

### Backend on Render

Point Render at the `backend` folder.

- Runtime: `Java`
- Build command:

```bash
mvn clean package -DskipTests
```

- Start command:

```bash
java -jar target/cardekho-ai-buyer-backend.jar
```

- Health check path: `/health`

Set these environment variables in Render:

- `GEMINI_API_KEY` = your Google AI Studio key
- `CORS_ALLOWED_ORIGINS` = your Vercel frontend URL
  - Example: `https://your-app.vercel.app`

Render will provide `PORT` automatically and the app is already configured to use it.

You can also use the root [render.yaml](D:\Cardekho\render.yaml) blueprint if you want Render to prefill the backend service config.

### Frontend on Vercel

Point Vercel at the `frontend` folder.

- Framework preset: `Vite`
- Build command: `npm run build`
- Output directory: `dist`

Set this environment variable in Vercel:

- `VITE_API_BASE_URL` = your Render backend URL with `/api`
  - Example: `https://your-render-service.onrender.com/api`

The frontend already includes [vercel.json](D:\Cardekho\frontend\vercel.json) so React Router routes work after deployment.

### Deployment order

1. Deploy backend on Render first.
2. Copy the Render backend URL.
3. Set `VITE_API_BASE_URL` in Vercel.
4. Deploy frontend on Vercel.
5. Add the final Vercel URL back into Render as `CORS_ALLOWED_ORIGINS`.

## Build checks run

- Backend:

```powershell
cd backend
mvn "-Dmaven.repo.local=.m2repo" test
```

- Frontend:

```powershell
cd frontend
npm install
npm run build
```

## Project structure

- `backend/` Spring Boot API
- `frontend/` React app
- `backend/src/main/resources/cars.json` seeded car catalog with 50 entries
- `start-local.ps1` one-command local startup

## README questions from the assignment

### What did you build and why?

I built a buyer-assistant MVP that turns a few preference inputs into a shortlist of three cars, explains the reasoning, highlights trade-offs, and allows saving shortlists. I chose this because it is the shortest path to buyer confidence and can be shipped cleanly in a 2-3 hour window.

### What did you deliberately cut?

I cut auth, real catalog ingestion, advanced analytics, and polished dealership workflows. Those are useful later, but not necessary to prove product judgment or shipping instinct in the time box.

### What’s your tech stack and why did you pick it?

React/Vite gave me a fast frontend setup and Spring Boot gave me a solid Java backend with clear structure. H2 kept persistence simple. Gemini was used both to interpret natural-language buyer intent and to generate shortlist explanations, while keeping cost at zero for MVP use.

### What did you delegate to AI tools vs. do manually? Where did the tools help most? Where did they get in the way?

AI was best for accelerating scaffolding, structuring the feature slices, and drafting the UI/backend boilerplate quickly. Manual work mattered most in narrowing scope, adjusting the recommendation logic, keeping the data model sensible, and making sure the product stayed focused instead of turning into a bloated car portal.

## How the AI works in the product

There are now two AI touchpoints:

1. `POST /api/ai/intake`
   - The user can type a messy buyer brief like: "Budget 15 lakh, Bangalore traffic, automatic SUV, parents in rear, safety first."
   - The backend sends that text to Gemini and asks for structured JSON preferences.
   - If Gemini is unavailable or no API key is set, the backend falls back to deterministic keyword/rule extraction.

2. `POST /api/recommendations`
   - The recommendation ranking itself is still deterministic and transparent.
   - After the top 5 cars are selected, Gemini writes a concise buyer-facing explanation of the shortlist and trade-offs.
   - Gemini also generates short per-car "Why this car?" explanations for the UI.

This keeps the system practical: AI handles ambiguity, while the backend keeps final ranking logic predictable and easy to explain.

### If you had another 4 hours, what would you add?

- More realistic catalog data from CSV/JSON
- Better prompt personalization and grounding
- On-road price breakdown
- Better comparison visualizations
- A deployed version
- A few high-value tests around scoring and shortlist persistence

## Screen recording reminder

The assignment requires an unedited or lightly fast-forwarded recording of the entire build process. Add that link, your repo link, and your live URL or local run instructions in the submission email.
