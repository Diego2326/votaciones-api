# Votaciones API

Backend en Kotlin + Spring Boot para torneos, encuestas y brackets competitivos.

Este README esta orientado a quien va a construir el frontend en React + Vite.

## Stack

- Backend: Kotlin + Spring Boot
- DB: PostgreSQL
- Auth organizador/admin: JWT
- Acceso votante: PIN/QR + session token por torneo
- Tiempo real: WebSocket + STOMP
- Docs API: Swagger / OpenAPI

## URLs locales

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Levantar entorno

### Base de datos + API con Docker

```bash
docker compose up -d
```

### Solo PostgreSQL + API local

```bash
docker compose up -d postgres
./gradlew bootRun
```

## Modelo de frontend recomendado

Se recomienda separar dos apps o al menos dos shells principales:

1. Panel organizador
   Usa JWT con login normal.

2. Web votantes
   Usa acceso por PIN o QR.
   No depende de JWT salvo en torneos configurados como `EMAIL_PASSWORD`.

## Tipos de acceso para votantes

Cada torneo tiene un `accessMode`:

- `ANONYMOUS`
  El usuario entra por QR o PIN y obtiene una sesion sin identificarse.

- `DISPLAY_NAME`
  El usuario entra por PIN o QR y envia solo su nombre.

- `EMAIL_PASSWORD`
  El usuario entra por PIN o QR y autentica con correo y password.
  Si no existe usuario, el backend lo crea automaticamente con rol `VOTER`.

## Conceptos clave para frontend

### 1. JWT de organizador/admin

Se usa para:

- crear torneos
- editar torneos
- configurar acceso del torneo
- administrar rondas, matches, participantes

Header:

```http
Authorization: Bearer <accessToken>
```

### 2. Session token del votante

Se usa para:

- votar
- consultar si ya voto
- mantener presencia en el torneo

Header:

```http
X-Tournament-Session: <sessionToken>
```

Este token no reemplaza JWT. Es una sesion de acceso al torneo.

Recomendacion frontend:

- guardar `accessToken` del organizador en almacenamiento seguro del panel
- guardar `sessionToken` del votante en `localStorage` o `sessionStorage`
- asociar `sessionToken` al `tournamentId`

## Respuesta base de la API

La API responde en este envelope:

```json
{
  "success": true,
  "message": "optional",
  "data": {},
  "timestamp": "2026-04-08T15:00:00Z"
}
```

Errores:

```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "path": "/api/v1/join/name",
  "errors": [
    {
      "field": "displayName",
      "message": "must not be blank"
    }
  ],
  "timestamp": "2026-04-08T15:00:00Z"
}
```

## Flujo del panel organizador

### Login

```http
POST /api/v1/auth/login
```

Body:

```json
{
  "usernameOrEmail": "admin@example.com",
  "password": "Password123!"
}
```

Respuesta importante:

- `data.tokens.accessToken`
- `data.tokens.refreshToken`
- `data.user`

### Crear torneo

```http
POST /api/v1/tournaments
Authorization: Bearer <token>
```

Body ejemplo:

```json
{
  "title": "Mejor pelicula",
  "description": "Bracket de peliculas",
  "type": "BRACKET",
  "accessMode": "DISPLAY_NAME",
  "startAt": null,
  "endAt": null
}
```

### Configurar acceso del torneo

Obtener config:

```http
GET /api/v1/tournaments/{tournamentId}/access
Authorization: Bearer <token>
```

Respuesta:

```json
{
  "success": true,
  "data": {
    "tournamentId": "uuid",
    "mode": "DISPLAY_NAME",
    "joinPin": "483271",
    "qrToken": "token-largo",
    "joinUrl": "http://localhost:3000/join/token-largo"
  }
}
```

Actualizar modo:

```http
PATCH /api/v1/tournaments/{tournamentId}/access
Authorization: Bearer <token>
```

Body:

```json
{
  "mode": "ANONYMOUS"
}
```

Regenerar PIN + QR:

```http
PATCH /api/v1/tournaments/{tournamentId}/regenerate-pin
Authorization: Bearer <token>
```

### QR en frontend

El backend no genera la imagen QR.

El backend genera:

- `qrToken`
- `joinUrl`

El frontend del organizador debe renderizar un QR con `joinUrl`.

En React + Vite puedes usar una libreria como:

- `qrcode.react`
- `react-qr-code`

Recomendacion:

- mostrar tambien el PIN grande en pantalla
- mostrar QR y PIN simultaneamente

## Flujo de la web votantes

## Ruta de entrada recomendada

Recomendacion en React Router:

- `/`
  pantalla para ingresar PIN

- `/join/:qrToken`
  pantalla abierta por QR

- `/tournament/:tournamentId/lobby`
  lobby del torneo

- `/tournament/:tournamentId/match/:matchId`
  pantalla de votacion

## Escenario A: entrar por PIN

### 1. Resolver torneo por PIN

```http
POST /api/v1/join/pin
```

Body:

```json
{
  "pin": "483271"
}
```

Respuesta:

```json
{
  "success": true,
  "data": {
    "tournamentId": "uuid",
    "mode": "DISPLAY_NAME",
    "joinPin": "483271",
    "qrToken": "token-largo",
    "joinUrl": "http://localhost:3000/join/token-largo"
  }
}
```

Con `mode`, el frontend decide la siguiente pantalla:

- `ANONYMOUS` -> entrar directamente
- `DISPLAY_NAME` -> pedir nombre
- `EMAIL_PASSWORD` -> pedir correo y password

## Escenario B: entrar por QR

### 1. Resolver torneo por QR

```http
POST /api/v1/join/qr/info
```

Body:

```json
{
  "qrToken": "token-largo"
}
```

El frontend usa la misma logica por `mode`.

## Crear sesion votante

### Modo `DISPLAY_NAME`

```http
POST /api/v1/join/name
```

Con PIN:

```json
{
  "pin": "483271",
  "displayName": "Diego"
}
```

Con QR:

```json
{
  "qrToken": "token-largo",
  "displayName": "Diego"
}
```

### Modo `ANONYMOUS`

```http
POST /api/v1/join/qr
```

Body:

```json
{
  "qrToken": "token-largo"
}
```

### Modo `EMAIL_PASSWORD`

```http
POST /api/v1/join/auth
```

Con PIN:

```json
{
  "pin": "483271",
  "email": "voter@example.com",
  "password": "Password123!",
  "firstName": "Diego",
  "lastName": "Perez"
}
```

Con QR:

```json
{
  "qrToken": "token-largo",
  "email": "voter@example.com",
  "password": "Password123!"
}
```

### Respuesta de sesion

```json
{
  "success": true,
  "data": {
    "tournamentId": "uuid",
    "tournamentTitle": "Mejor pelicula",
    "mode": "DISPLAY_NAME",
    "sessionToken": "token-largo",
    "displayName": "Diego",
    "userId": null,
    "joinedAt": "2026-04-08T15:00:00Z",
    "expiresAt": null
  }
}
```

Guardar:

- `sessionToken`
- `tournamentId`
- `displayName`

## Mantener sesion de votante

```http
GET /api/v1/join/me
X-Tournament-Session: <sessionToken>
```

Esto sirve para:

- restaurar sesion al refrescar la pagina
- actualizar `lastSeenAt`
- validar que la sesion sigue viva

## Obtener torneos, rondas, matches y participantes

Lecturas publicas:

```http
GET /api/v1/tournaments
GET /api/v1/tournaments/{id}
GET /api/v1/tournaments/{tournamentId}/participants
GET /api/v1/tournaments/{tournamentId}/rounds
GET /api/v1/rounds/{id}
GET /api/v1/rounds/{roundId}/matches
GET /api/v1/matches/{id}
GET /api/v1/matches/{matchId}/results
GET /api/v1/rounds/{roundId}/results
GET /api/v1/tournaments/{tournamentId}/results
```

## Votar

```http
POST /api/v1/matches/{matchId}/vote
X-Tournament-Session: <sessionToken>
```

Body:

```json
{
  "selectedParticipantId": "participant-uuid"
}
```

Respuesta:

```json
{
  "success": true,
  "message": "Vote recorded",
  "data": {
    "id": "vote-uuid",
    "tournamentId": "uuid",
    "roundId": "uuid",
    "matchId": "uuid",
    "voterId": null,
    "joinSessionId": "uuid",
    "selectedParticipantId": "uuid",
    "createdAt": "2026-04-08T15:00:00Z"
  }
}
```

## Saber si ya voto

```http
GET /api/v1/matches/{matchId}/my-vote
X-Tournament-Session: <sessionToken>
```

Respuesta:

```json
{
  "success": true,
  "data": {
    "hasVoted": true,
    "selectedParticipantId": "uuid",
    "votedAt": "2026-04-08T15:00:00Z"
  }
}
```

## Reglas de negocio que el frontend debe asumir

- no se puede votar dos veces en el mismo match con la misma sesion
- la ronda debe estar `OPEN`
- el torneo debe estar `ACTIVE`
- el match debe estar `OPEN`
- una sesion de torneo solo sirve para su propio torneo

## WebSocket en frontend

Endpoint:

- SockJS/STOMP endpoint: `/ws`

Topics disponibles:

- `/topic/tournament/{tournamentId}`
- `/topic/tournament/{tournamentId}/round/{roundId}`

Eventos posibles:

- `TOURNAMENT_UPDATED`
- `ROUND_OPENED`
- `ROUND_CLOSED`
- `VOTE_COUNT_UPDATED`
- `RESULTS_PUBLISHED`
- `PARTICIPATION_UPDATED`

Payload base:

```json
{
  "eventType": "VOTE_COUNT_UPDATED",
  "tournamentId": "uuid",
  "roundId": "uuid",
  "matchId": "uuid",
  "message": "Vote count updated",
  "payload": {
    "totalVotes": 20
  },
  "emittedAt": "2026-04-08T15:00:00Z"
}
```

### Recomendacion React

- usa STOMP client
- al entrar al lobby del torneo, suscribete a `/topic/tournament/{tournamentId}`
- al abrir una ronda concreta, suscribete tambien a `/topic/tournament/{tournamentId}/round/{roundId}`
- ante `VOTE_COUNT_UPDATED`, refresca resultados o contador
- ante `ROUND_OPENED`, `ROUND_CLOSED`, `RESULTS_PUBLISHED`, invalida cache y vuelve a consultar API

## Sugerencia de estructura React + Vite

```text
src/
  app/
    router.tsx
    providers.tsx
  api/
    http.ts
    auth.ts
    tournaments.ts
    join.ts
    votes.ts
    websocket.ts
  features/
    organizer/
      auth/
      tournaments/
      rounds/
      matches/
      participants/
      access/
    voter/
      join/
      lobby/
      voting/
      results/
  components/
  hooks/
  lib/
  types/
```

## Cliente HTTP recomendado

- `fetch` o `axios`
- crear dos helpers:

1. `authorizedClient`
   agrega `Authorization: Bearer <jwt>`

2. `sessionClient`
   agrega `X-Tournament-Session: <sessionToken>`

## Estados que el frontend debe modelar

### Tournament

- `DRAFT`
- `PUBLISHED`
- `ACTIVE`
- `PAUSED`
- `CLOSED`
- `FINISHED`
- `CANCELLED`

### Round

- `PENDING`
- `OPEN`
- `CLOSED`
- `PROCESSING`
- `PUBLISHED`

### Match

- `PENDING`
- `OPEN`
- `CLOSED`
- `RESOLVED`
- `TIED`
- `CANCELLED`

## Swagger

Para descubrir payloads reales:

- `http://localhost:8080/swagger-ui.html`

## Observaciones importantes para frontend

- el `joinPin` y `qrToken` no vienen en el detalle publico del torneo
- solo el organizador obtiene esos datos desde `/tournaments/{id}/access`
- el QR debe apuntar al frontend, no al backend, usando `joinUrl`
- si el usuario entra por QR, el frontend debe parsear `qrToken` desde la URL y llamar `/api/v1/join/qr/info`

## Recomendacion de implementacion por fases

### Fase 1

- login organizador
- CRUD torneos
- mostrar PIN + QR

### Fase 2

- entrada votante por PIN
- modos `DISPLAY_NAME` y `ANONYMOUS`
- lobby y lista de matches

### Fase 3

- voto por sesion
- resultados y estado de voto
- realtime con WebSocket

### Fase 4

- modo `EMAIL_PASSWORD`
- reconexion de sesion
- UX fina y manejo de errores

## Estado actual del backend

Implementado y probado:

- auth JWT
- torneos, rondas, matches, participantes
- acceso por PIN/QR
- sesion de torneo
- voto por sesion
- auditoria
- WebSocket
- Swagger

Verificacion:

```bash
./gradlew test
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```
