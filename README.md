# Rated Rock-Paper-Scissors Web Application 🎮⚡

A full-stack, real-time **Rock-Paper-Scissors** game with matchmaking, ranked Elo scoring, and live updates. This repository contains both the **Spring Boot WebSocket backend server** and the **React frontend client**, integrated with Supabase for user authentication and persistent stats.

---

## 🚀 Project Overview

This project delivers an interactive, competitive Rock-Paper-Scissors game experience, enabling players to:

- **Sign up/login** with secure authentication (Supabase).
- **Join matchmaking queues** to find opponents in real-time.
- **Play matches live** with instant game state updates via WebSocket.
- **Track player stats** like wins, losses, draws, and Elo rating.
- **View results** with elo changes and game history.

---

## 🖥️ Client (Frontend)

### Technologies

- **React** with functional components & hooks (`useState`, `useEffect`)
- **React Router** for nested routing (Dashboard, Play, Profile, User pages)
- **WebSocket API** for real-time game communication
- **Tailwind CSS** for responsive and modern UI styling
- **Supabase Auth** for user sign-up, login, and session management
- **State management** for loading states, game results, and user feedback
- Responsive UX with loading animations and error handling

### Features

- User authentication (Sign Up / Sign In)
- Dashboard with nested routes: Play, Profile, Leaderboard, User profiles
- Real-time matchmaking queue display & game interface
- Elo and game result display with animations
- Clean UI components for usability and accessibility
- Error and loading state visual feedback during network requests

---

## ⚙️ Server (Backend)

### Technologies

- **Java 17** with **Spring Boot 3** for REST and WebSocket server
- **Spring WebSocket** for bidirectional, real-time client-server communication
- **Supabase** backend for user data and game persistence (PostgreSQL)
- **ConcurrentHashMap & ConcurrentLinkedQueue** for thread-safe matchmaking & session management
- **Jackson ObjectMapper** for JSON serialization/deserialization
- **SLF4J with Logback** for structured logging and debugging

### Features

- Real-time player matchmaking with queue management
- Concurrent game sessions with player moves and result determination
- Elo rating calculation and updating wins/losses/draws on game end
- Session cleanup on user disconnect, removing stale game data
- Broadcasting game updates and lobby statuses to clients instantly
- API integration with Supabase to persist player stats and game results
- Error handling with descriptive client messages on invalid actions

---

## ⚡ How to Run

### Backend Server

1. Set up Supabase project, get API keys.
2. Configure keys and URLs in backend environment.
3. Build and run backend:

```bash
mvn clean install
mvn spring-boot:run
```

### Frontend Client
1. Configure Supabase URL and keys in `.env`.

2. Run React app:

```bash
npm install
npm start
```

