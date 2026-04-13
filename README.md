CarDekho AI Buyer Guide

This is a small full-stack project where I tried to solve one simple problem:
help a confused car buyer quickly get a shortlist of good options.

What I built

I created a React + Spring Boot app with these features:

User can type their requirements in normal language
A small quiz to understand buyer needs better
AI suggests a shortlist of cars
Each car has a “Why this car?” explanation
Simple comparison view
Option to save shortlists

Backend features:

Car data loaded from a JSON file
AI converts messy user input into structured preferences
Recommendation logic with scoring + confidence
Gemini API used for explanations (fallback if API not available)
H2 database to store saved shortlists
Why I built this

The main idea was to solve this flow:

User is confused
User explains needs in simple words
System understands it
Shows 3 good options
Explains why
User saves shortlist

I focused only on this core journey instead of adding too many features.

What I skipped (on purpose)

To keep it simple and fast, I didn’t include:

Login/auth
Large real-world car data
Dealer/location data
Advanced comparison charts
Full testing setup
Deployment automation
Tech stack (and why)
Frontend: React + Vite
→ Fast and easy to build UI
Backend: Spring Boot + JPA + H2
→ Simple and structured Java backend
AI: Gemini API
→ Free and easy to use for MVP
AI setup

Before running backend, set this:

$env:GEMINI_API_KEY="your_key_here"

If no key is added, app still works using fallback logic.

Run locally
Easy way
.\start-local.ps1

This starts:

Backend → http://localhost:8080
Frontend → http://localhost:5173
Manual way

Backend:

cd backend
mvn "-Dmaven.repo.local=.m2repo" spring-boot:run

Frontend:

cd frontend
npm install
npm run dev
Deployment
Backend (Render)
Use Docker (because Java runtime not directly listed)
Health check: /health

Environment variables:

GEMINI_API_KEY
CORS_ALLOWED_ORIGINS → your frontend URL
Frontend (Vercel)
Framework: Vite
Build: npm run build
Output: dist

Env variable:

VITE_API_BASE_URL → your backend /api URL
Deployment steps
Deploy backend
Copy backend URL
Add it in Vercel
Deploy frontend
Add frontend URL in backend CORS
Project structure
backend/ → Spring Boot API
frontend/ → React app
cars.json → car dataset
start-local.ps1 → run everything
