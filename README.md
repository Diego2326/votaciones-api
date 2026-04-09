# Votaciones API

Backend en Kotlin + Spring Boot para torneos, encuestas y brackets competitivos.

Licencia: MIT. Ver [LICENSE](./LICENSE).

## Stack

- Kotlin + Spring Boot
- PostgreSQL + Flyway
- JWT para admin y organizer
- Session token por torneo para votantes
- WebSocket + STOMP + SockJS para eventos en tiempo real
- Swagger / OpenAPI para exploracion manual

## URLs locales

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Healthcheck: `http://localhost:8080/actuator/health`
- WebSocket SockJS/STOMP: `ws://localhost:8080/ws`

## Levantar el proyecto

### Con Docker

```bash
docker compose up -d
```

### PostgreSQL en Docker + API local

```bash
docker compose up -d postgres
./gradlew bootRun
```

### Tests

```bash
./gradlew test
```

## Modelos de autenticacion

La API usa tres modelos distintos:

### 1. Publico

No requiere headers de autenticacion.

### 2. JWT

Header:

```http
Authorization: Bearer <accessToken>
```

Se usa para:

- auth del panel
- CRUD de torneos
- acceso de administracion
- usuarios
- auditoria

Nota: un `ORGANIZER` solo puede administrar torneos creados por el mismo. Un `ADMIN` puede administrar cualquiera.

### 3. Sesion de torneo

Header:

```http
X-Tournament-Session: <sessionToken>
```

Se usa para:

- votar
- consultar el voto propio
- restaurar y mantener viva la sesion del votante

## Envelope de respuestas

Exito:

```json
{
  "success": true,
  "message": "optional",
  "data": {},
  "timestamp": "2026-04-09T12:00:00Z"
}
```

Error:

```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "path": "/api/v1/tournaments",
  "errors": [
    {
      "field": "title",
      "message": "is required"
    }
  ],
  "timestamp": "2026-04-09T12:00:00Z"
}
```

## Enums relevantes

### TournamentType

- `ELIMINATION`
- `ROUND_BASED`
- `POLL`
- `BRACKET`

### TournamentStatus

- `DRAFT`
- `PUBLISHED`
- `ACTIVE`
- `PAUSED`
- `CLOSED`
- `FINISHED`
- `CANCELLED`

### TournamentAccessMode

- `EMAIL_PASSWORD`
- `DISPLAY_NAME`
- `ANONYMOUS`

### RoundStatus

- `PENDING`
- `OPEN`
- `CLOSED`
- `PROCESSING`
- `PUBLISHED`

### MatchStatus

- `PENDING`
- `OPEN`
- `CLOSED`
- `RESOLVED`
- `TIED`
- `CANCELLED`

### RoleName

- `ADMIN`
- `ORGANIZER`
- `VOTER`

## Referencia completa de endpoints

Leyenda:

- `Publico`: sin autenticacion
- `JWT`: requiere `Authorization: Bearer <token>`
- `Sesion`: requiere `X-Tournament-Session: <token>`

### Auth

| Metodo | Ruta | Auth | Body |
| --- | --- | --- | --- |
| GET | `/api/v1/auth/register` | Publico | Sin body |
| POST | `/api/v1/auth/register` | Publico | `username`, `email`, `password`, `firstName`, `lastName` |
| POST | `/api/v1/auth/login` | Publico | `usernameOrEmail`, `password` |
| POST | `/api/v1/auth/refresh` | Publico | `refreshToken` |
| GET | `/api/v1/auth/me` | JWT | Sin body |

#### POST /api/v1/auth/register

```json
{
  "username": "organizer01",
  "email": "organizer@example.com",
  "password": "Password123!",
  "firstName": "Diego",
  "lastName": "Perez"
}
```

Reglas:

- `username`: 3 a 64 caracteres
- `email`: email valido, maximo 128
- `password`: 8 a 72 caracteres
- `firstName`: maximo 100
- `lastName`: maximo 100

#### POST /api/v1/auth/login

```json
{
  "usernameOrEmail": "organizer01",
  "password": "Password123!"
}
```

#### POST /api/v1/auth/refresh

```json
{
  "refreshToken": "token"
}
```

### Torneos

| Metodo | Ruta | Auth | Body / Query |
| --- | --- | --- | --- |
| POST | `/api/v1/tournaments` | JWT (`ADMIN`, `ORGANIZER`) | `title`, `description?`, `type`, `startAt?`, `endAt?`, `accessMode?` |
| GET | `/api/v1/tournaments` | Publico | Query: `status?`, `page=0`, `size=20` |
| GET | `/api/v1/tournaments/{id}` | Publico | Sin body |
| PUT | `/api/v1/tournaments/{id}` | JWT (`ADMIN`, `ORGANIZER`) | Igual a create, pero `accessMode` es requerido |
| PATCH | `/api/v1/tournaments/{id}/publish` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/tournaments/{id}/activate` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/tournaments/{id}/pause` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/tournaments/{id}/close` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |

#### POST /api/v1/tournaments

```json
{
  "title": "Mejor pelicula 2026",
  "description": "Bracket de peliculas",
  "type": "BRACKET",
  "startAt": "2026-04-10T18:00:00Z",
  "endAt": "2026-04-17T18:00:00Z",
  "accessMode": "DISPLAY_NAME"
}
```

Reglas:

- `title`: requerido, no vacio, maximo 160
- `description`: opcional, maximo 4000
- `type`: requerido
- `startAt`: opcional, `Instant` ISO-8601
- `endAt`: opcional, `Instant` ISO-8601
- `accessMode`: opcional, por defecto `ANONYMOUS`
- `endAt` no puede ser menor que `startAt`

Reglas de negocio:

- publicar requiere al menos 2 participantes activos
- un torneo `ACTIVE`, `CLOSED`, `FINISHED` o `CANCELLED` ya no es editable
- `activate` solo permite pasar desde `PUBLISHED` o `PAUSED`
- `pause` solo permite pasar desde `ACTIVE`
- `close` solo permite pasar desde `ACTIVE`, `PAUSED` o `PUBLISHED`

### Acceso al torneo y flujo de join

| Metodo | Ruta | Auth | Body |
| --- | --- | --- | --- |
| GET | `/api/v1/tournaments/{tournamentId}/access` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/tournaments/{tournamentId}/access` | JWT (`ADMIN`, `ORGANIZER`) | `mode` |
| PATCH | `/api/v1/tournaments/{tournamentId}/regenerate-pin` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| POST | `/api/v1/join/pin` | Publico | `pin` |
| POST | `/api/v1/join/qr/info` | Publico | `qrToken` |
| POST | `/api/v1/join/name` | Publico | `displayName` y uno de `pin` o `qrToken` |
| POST | `/api/v1/join/qr` | Publico | `qrToken` |
| POST | `/api/v1/join/auth` | Publico | `email`, `password` y uno de `pin` o `qrToken` |
| GET | `/api/v1/join/me` | Sesion | Sin body |

#### PATCH /api/v1/tournaments/{tournamentId}/access

```json
{
  "mode": "ANONYMOUS"
}
```

#### POST /api/v1/join/pin

```json
{
  "pin": "483271"
}
```

Regla: `pin` entre 4 y 12 caracteres.

#### POST /api/v1/join/qr/info

```json
{
  "qrToken": "token-largo"
}
```

#### POST /api/v1/join/name

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

Reglas:

- `displayName`: requerido, maximo 120
- `pin`: opcional, pero si se usa debe medir 4 a 12
- enviar `pin` o `qrToken`

#### POST /api/v1/join/qr

```json
{
  "qrToken": "token-largo"
}
```

#### POST /api/v1/join/auth

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

Reglas:

- `email`: requerido y valido
- `password`: requerido, 8 a 72
- `firstName`: opcional, maximo 100
- `lastName`: opcional, maximo 100
- enviar `pin` o `qrToken`

#### Respuesta relevante de join

```json
{
  "success": true,
  "message": "Tournament session created",
  "data": {
    "tournamentId": "uuid",
    "tournamentTitle": "Mejor pelicula 2026",
    "mode": "DISPLAY_NAME",
    "sessionToken": "token-largo",
    "displayName": "Diego",
    "userId": null,
    "joinedAt": "2026-04-09T12:00:00Z",
    "expiresAt": null
  }
}
```

Guardar como minimo:

- `sessionToken`
- `tournamentId`
- `displayName`

### Participantes

| Metodo | Ruta | Auth | Body |
| --- | --- | --- | --- |
| POST | `/api/v1/tournaments/{tournamentId}/participants` | JWT (`ADMIN`, `ORGANIZER`) | `name`, `description?`, `imageUrl?`, `active?` |
| GET | `/api/v1/tournaments/{tournamentId}/participants` | JWT | Sin body |
| PUT | `/api/v1/participants/{id}` | JWT (`ADMIN`, `ORGANIZER`) | `name`, `description?`, `imageUrl?`, `active?` |
| DELETE | `/api/v1/participants/{id}` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |

#### Body de create/update participante

```json
{
  "name": "Pelicula A",
  "description": "Descripcion opcional",
  "imageUrl": "https://example.com/poster.jpg",
  "active": true
}
```

Reglas:

- `name`: requerido, maximo 160
- `description`: opcional, maximo 4000
- `imageUrl`: opcional, maximo 500
- `DELETE` hace soft delete: el participante se marca inactivo

### Rondas

| Metodo | Ruta | Auth | Body |
| --- | --- | --- | --- |
| POST | `/api/v1/tournaments/{tournamentId}/rounds` | JWT (`ADMIN`, `ORGANIZER`) | `name`, `roundNumber`, `opensAt?`, `closesAt?` |
| GET | `/api/v1/tournaments/{tournamentId}/rounds` | JWT | Sin body |
| GET | `/api/v1/rounds/{id}` | Publico | Sin body |
| PATCH | `/api/v1/rounds/{id}/open` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/rounds/{id}/close` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/rounds/{id}/process` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |
| PATCH | `/api/v1/rounds/{id}/publish-results` | JWT (`ADMIN`, `ORGANIZER`) | Sin body |

#### POST /api/v1/tournaments/{tournamentId}/rounds

```json
{
  "name": "Cuartos de final",
  "roundNumber": 1,
  "opensAt": "2026-04-10T18:00:00Z",
  "closesAt": "2026-04-10T21:00:00Z"
}
```

Reglas:

- `name`: requerido, maximo 120
- `roundNumber`: requerido, entero positivo
- `closesAt` no puede ser menor que `opensAt`
- no se puede repetir `roundNumber` dentro del mismo torneo
- `open` solo funciona en rondas `PENDING`
- `close` solo funciona en rondas `OPEN`
- `process` solo funciona en rondas `CLOSED` o `PROCESSING`
- `publish-results` solo funciona en rondas `PROCESSING`

### Matches

| Metodo | Ruta | Auth | Body |
| --- | --- | --- | --- |
| POST | `/api/v1/rounds/{roundId}/matches` | JWT (`ADMIN`, `ORGANIZER`) | `autoGenerate` y/o `matches[]` |
| GET | `/api/v1/rounds/{roundId}/matches` | JWT | Sin body |
| GET | `/api/v1/matches/{id}` | Publico | Sin body |
| PATCH | `/api/v1/matches/{id}/winner` | JWT (`ADMIN`, `ORGANIZER`) | `winnerId` |

#### POST /api/v1/rounds/{roundId}/matches manual

```json
{
  "autoGenerate": false,
  "matches": [
    {
      "participantAId": "uuid-a",
      "participantBId": "uuid-b"
    },
    {
      "participantAId": "uuid-c",
      "participantBId": "uuid-d"
    }
  ]
}
```

#### POST /api/v1/rounds/{roundId}/matches auto

```json
{
  "autoGenerate": true,
  "matches": []
}
```

Reglas:

- si `autoGenerate` es `true`, el backend empareja participantes activos de 2 en 2
- auto-generacion requiere cantidad par de participantes activos
- si `autoGenerate` es `false`, debes enviar `matches`
- la ronda debe estar en `PENDING`
- un match no puede usar el mismo participante dos veces
- ambos participantes deben pertenecer al torneo y estar activos

#### PATCH /api/v1/matches/{id}/winner

```json
{
  "winnerId": "participant-uuid"
}
```

### Votos y resultados

| Metodo | Ruta | Auth | Body |
| --- | --- | --- | --- |
| POST | `/api/v1/matches/{matchId}/vote` | Sesion | `selectedParticipantId` |
| GET | `/api/v1/matches/{matchId}/results` | Publico | Sin body |
| GET | `/api/v1/rounds/{roundId}/results` | Publico | Sin body |
| GET | `/api/v1/tournaments/{tournamentId}/results` | JWT | Sin body |
| GET | `/api/v1/matches/{matchId}/my-vote` | Sesion | Sin body |

#### POST /api/v1/matches/{matchId}/vote

```json
{
  "selectedParticipantId": "participant-uuid"
}
```

Reglas:

- `selectedParticipantId`: requerido
- la sesion solo sirve para su propio torneo
- no se puede votar dos veces en el mismo match con la misma sesion
- el torneo debe estar `ACTIVE`
- la ronda debe estar `OPEN`
- el match debe estar `OPEN`

#### GET /api/v1/matches/{matchId}/my-vote

Respuesta tipica:

```json
{
  "success": true,
  "data": {
    "hasVoted": true,
    "selectedParticipantId": "uuid",
    "votedAt": "2026-04-09T12:00:00Z"
  }
}
```

### Usuarios

Todos los endpoints de esta seccion requieren JWT con rol `ADMIN`.

| Metodo | Ruta | Auth | Body / Query |
| --- | --- | --- | --- |
| GET | `/api/v1/users` | JWT (`ADMIN`) | Query: `page=0`, `size=20` |
| GET | `/api/v1/users/{id}` | JWT (`ADMIN`) | Sin body |
| PATCH | `/api/v1/users/{id}/status` | JWT (`ADMIN`) | `enabled` |
| PATCH | `/api/v1/users/{id}/roles` | JWT (`ADMIN`) | `roles[]` |

#### PATCH /api/v1/users/{id}/status

```json
{
  "enabled": true
}
```

#### PATCH /api/v1/users/{id}/roles

```json
{
  "roles": ["ORGANIZER", "VOTER"]
}
```

### Auditoria

| Metodo | Ruta | Auth | Query |
| --- | --- | --- | --- |
| GET | `/api/v1/audit` | JWT (`ADMIN`) | `page=0`, `size=20` |
| GET | `/api/v1/tournaments/{tournamentId}/audit` | JWT (`ADMIN`, `ORGANIZER`) | `page=0`, `size=20` |

Nota: en la auditoria por torneo, un `ORGANIZER` solo puede consultar torneos que administra.

### Docs, health y WebSocket

| Metodo | Ruta | Auth | Uso |
| --- | --- | --- | --- |
| GET | `/swagger-ui.html` | Publico | Swagger UI |
| GET | `/swagger-ui/**` | Publico | Assets de Swagger |
| GET | `/v3/api-docs/**` | Publico | OpenAPI JSON |
| GET | `/actuator/health` | Publico | Healthcheck |
| GET | `/ws` | Publico | Handshake SockJS/STOMP |

## WebSocket en tiempo real

Endpoint de conexion:

- `/ws`

Broker destinations:

- `/topic/**`
- `/queue/**`

Prefijo de envio del cliente:

- `/app/**`

Topics usados por el backend:

- `/topic/tournament/{tournamentId}`
- `/topic/tournament/{tournamentId}/round/{roundId}`

Eventos emitidos:

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
  "emittedAt": "2026-04-09T12:00:00Z"
}
```

## Flujo sugerido para frontend

### Panel organizer

1. `POST /api/v1/auth/register` o `POST /api/v1/auth/login`
2. `POST /api/v1/tournaments`
3. `POST /api/v1/tournaments/{tournamentId}/participants`
4. `POST /api/v1/tournaments/{tournamentId}/rounds`
5. `POST /api/v1/rounds/{roundId}/matches`
6. `PATCH /api/v1/tournaments/{tournamentId}/publish`
7. `PATCH /api/v1/tournaments/{tournamentId}/activate`
8. `GET /api/v1/tournaments/{tournamentId}/access`

### Web votantes

1. `POST /api/v1/join/pin` o `POST /api/v1/join/qr/info`
2. Segun `mode`, llamar `POST /api/v1/join/name`, `POST /api/v1/join/qr` o `POST /api/v1/join/auth`
3. Guardar `sessionToken`
4. `GET /api/v1/join/me` para restaurar sesion
5. `POST /api/v1/matches/{matchId}/vote`
6. `GET /api/v1/matches/{matchId}/my-vote`

## Observaciones importantes

- `GET /api/v1/tournaments/{tournamentId}/participants` no es publico en el estado actual; requiere JWT.
- `GET /api/v1/tournaments/{tournamentId}/rounds` no es publico en el estado actual; requiere JWT.
- `GET /api/v1/rounds/{roundId}/matches` no es publico en el estado actual; requiere JWT.
- `GET /api/v1/tournaments/{tournamentId}/results` no es publico en el estado actual; requiere JWT.
- el backend entrega `joinPin`, `qrToken` y `joinUrl` solo por el endpoint de acceso del torneo
- el frontend debe renderizar la imagen QR a partir de `joinUrl`
- `joinUrl` debe apuntar al frontend del votante, no al backend

## Swagger

Para inspeccionar request/response en vivo:

- `http://localhost:8080/swagger-ui.html`

## Licencia

Este proyecto se distribuye bajo la licencia MIT.
