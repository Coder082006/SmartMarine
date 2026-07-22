# Smart Marine Booking — Implementation Documentation

**Project:** Smart Marine Booking Mobile Application
**Team:** DIT — Class OD23IT, Group 06 (Module ITT 06214)
**Platform:** Native Android (Java), SQLite local storage + Node.js REST cloud backend
**Package:** `com.example.myapplication`

This document describes the **complete logic implemented** in the app and how each part maps to the project proposal. It is the up-to-date reference for the whole system (it replaces the earlier draft that only covered login/registration).

---

## 1. Overview

Smart Marine Booking lets passengers register, search for boats/ferries between Tanzanian ports (Dar es Salaam, Zanzibar, Mafia, Pemba, Mwanza, Bukoba), check live weather, view the route on a map, pay via (simulated) mobile money, and receive a digital ticket. All accounts and bookings are stored locally in **SQLite**; boat schedules are served **live from a cloud REST API**.

### Technologies used (mapped to the proposal)

| Proposal technology | How it is implemented |
|---|---|
| SQLite via `SQLiteOpenHelper` | `DatabaseHelper.java` — users, boats, bookings tables |
| ConstraintLayout + CardView | All screen layouts |
| LocationManager / GPS | `PortLocator.java` + `activity_search_boat` (auto-fills nearest port) |
| OpenWeatherMap API + Volley | `WeatherService.java` + `WeatherActivity` |
| Node.js REST API on Render | `/server` (Express) + `ApiClient.java` (Volley client) |
| Map view | `MapActivity` — OpenStreetMap + Leaflet in a WebView (no API key needed) |
| Payment integration | `PaymentActivity` — simulated mobile-money gateway |
| Digital ticket generation | `activity_ticket` — generates reference + QR, saves booking |

---

## 2. Full application flow

```
MainActivity (splash)
      │
      ▼
LoginActivity ──register──► RegisterActivity ──► (back to Login)
      │ login success
      ▼
HomeActivity ──────────────► (live weather card)
      │ "Search Boats"
      ▼
activity_search_boat  ── GPS auto-fills nearest port into "From"
      │ enter From / To / Date → SEARCH
      ▼
activity_available   ── LIVE schedules pulled from the cloud REST API
      │  • no boats  → "No boats available" message
      │  • cloud down → "Couldn't reach server, try again"
      │  • Check Weather → WeatherActivity
      │  • View Map     → MapActivity
      │ SELECT a boat
      ▼
PaymentActivity  ── simulated mobile-money payment (must succeed first)
      │ payment success
      ▼
activity_ticket  ── saves CONFIRMED booking to SQLite + shows reference/QR
      │
      ▼
activity_my_booking ── lists the user's saved bookings (from SQLite)
```

**Key rule:** a ticket is only generated **after** payment succeeds — the booking is written to the database inside the ticket screen, which can only be reached through the payment screen.

---

## 3. Data layer (SQLite)

### `DatabaseHelper.java`
Extends `SQLiteOpenHelper`. Database file **`SmartMarine.db`**, **version 2**. Three tables:

**`users`** — registered accounts
| column | type |
|---|---|
| id | INTEGER PK AUTOINCREMENT |
| full_name | TEXT |
| email | TEXT UNIQUE |
| phone | TEXT |
| password | TEXT |

**`boats`** — offline fallback schedules (seeded on first run with real Tanzanian routes: Dar↔Zanzibar, Dar↔Mafia, Zanzibar↔Pemba, Mwanza↔Bukoba)
| column | type |
|---|---|
| id, name, origin, destination, departure_time, arrival_time, price | |

**`bookings`** — saved tickets
| column | type |
|---|---|
| id, reference, user_email, passenger_name, boat_name, origin, destination, travel_date, departure_time, price, status, created_at | |

**Important methods**
- `registerUser(name, email, phone, password)` — inserts a user; returns `false` if the email already exists (UNIQUE constraint).
- `emailExists(email)` — case-insensitive check so Login can say *"No account found"* vs *"Incorrect password"* precisely.
- `loginUser(email, password)` — matches email **case-insensitively** (`LOWER(TRIM(email))`), password exactly.
- `getUserName(email)` — real passenger name for the ticket.
- `searchBoats(origin, destination)` — offline route lookup (case-insensitive), used only when the cloud is not configured.
- `createBooking(...)` — saves a booking with status `CONFIRMED` and returns a generated reference like `SMB-2026-1005`.
- `getBookingsForUser(email)` — the user's bookings, newest first, for *My Bookings*.

`onUpgrade` adds the boats/bookings tables **without dropping users**, so existing accounts survive upgrades.

### Model classes
- **`Boat.java`** — id, name, origin, destination, departureTime, arrivalTime, price; `getPriceText()` → `"TZS 35,000"`.
- **`Booking.java`** — reference, passengerName, boatName, origin, destination, travelDate, departureTime, price, status; helper `getRoute()` / `getDetail()`.

### Session
- **`SessionManager.java`** — a thin SharedPreferences wrapper (`login(email)`, `logout()`, `getEmail()`, `isLoggedIn()`). It only remembers **who is logged in** — all real data lives in SQLite.

---

## 4. Screen-by-screen logic

### MainActivity (splash)
Shows the logo, then moves to Login after a short delay.

### RegisterActivity
Validates input before saving: name ≥ 3 chars, email contains `@` and `.`, phone ≥ 10 digits, password ≥ 6 chars. On success calls `registerUser(...)` → SQLite, then goes to Login.

### LoginActivity
Validates the fields, then:
1. `emailExists(email)` → if false, *"No account found for this email. Please register first."*
2. else `loginUser(email, password)` → success starts a session (`session.login(email)`) and opens Home; failure shows *"Incorrect password."*

### HomeActivity
Landing screen. Loads **live weather** into the weather card via `WeatherService` and routes to Search / My Bookings. Logout clears the session.

### activity_search_boat
Collects **From**, **To**, and a **travel date** (DatePicker, no past dates). Uses **GPS (LocationManager)** through `PortLocator` to auto-fill the nearest port into the empty "From" field (with runtime location permission). Validates that From/To are present and different, then opens the Available screen with the route.

### activity_available (live schedule search)
**The cloud is the source of truth for schedules.**
- If the REST API is configured (`ApiClient.isConfigured()`), it fetches live boats for the route:
  - **boats returned** → binds them into the existing cards.
  - **empty result** → shows *"No boats available for [route]"* (it does **not** show local seed data).
  - **cloud unreachable** → shows *"Couldn't reach the schedule server… try again"* (again, no offline fallback).
- If no cloud URL is set, it falls back to the local SQLite `searchBoats(...)`.
- **Check Weather** button → opens `WeatherActivity` for the departure port.
- **View Map** button → opens `MapActivity` for the route.
- **SELECT** → opens `PaymentActivity` (not the ticket directly).

### PaymentActivity (simulated payment)
Shows a **booking summary** (boat, route, date, amount) and a **mobile-money** form (M-Pesa / Mixx-Tigo Pesa / Airtel Money / HaloPesa + phone number). On **PAY**:
1. Validates the number (≥ 10 digits).
2. Simulates contacting the network (~2.2s "processing…"), then reports success.
3. Forwards the booking details to the ticket screen in `"new"` mode and finishes.

> This is a **mock gateway** — no real money moves and no merchant account is required. It fully satisfies the proposal's "Payment integration" feature for demonstration.

### activity_ticket (digital ticket)
Two modes:
- **`new`** (from Payment) → calls `createBooking(...)`, saving a **CONFIRMED** booking, and displays the generated reference + QR + boat/route/date/passenger.
- **`view`** (from My Bookings) → just displays the passed-in reference and status.

### activity_my_booking
Loads `getBookingsForUser(session.getEmail())` from SQLite into the booking cards and refreshes in `onResume`. A green badge marks `CONFIRMED` bookings. Tapping a booking opens the ticket in `view` mode.

### WeatherActivity (full weather page)
Opened by *Check Weather*. Fetches live data from OpenWeatherMap for the departure port and shows the temperature, condition (with a weather emoji), and a **travel-advice banner** — **green** for good conditions, **orange** for rough seas (storm/rain/thunder/squall).

### MapActivity (route map)
Opened by *View Map*. Renders an **OpenStreetMap** map inside a WebView using **Leaflet**, plotting both ports with markers and a dashed route line between them. Uses `PortLocator.coordsFor(name)` for coordinates. Needs internet for the map tiles but **no API key**. Unknown ports show *"Map not available for this route."*

---

## 5. Networking / device services

### `ApiClient.java` (Volley REST client)
Talks to the Node.js API. `BASE_URL` holds the deployed Render URL; `isConfigured()` guards every call so the app never crashes if it is unset.
- `GET /api/boats?from=&to=` → live schedules (`searchBoats`)
- `POST /api/bookings` → create a booking in the cloud (`createBooking`)
- `GET /api/bookings?email=` → a user's cloud bookings (`getBookings`)

### `WeatherService.java` (OpenWeatherMap via Volley)
`fetchByCity(context, city, callback)` calls the Current Weather API and returns temperature, condition, and a `goodForTravel` flag. `hasApiKey()` guards the call. *(Fixed: the API key previously had a trailing space that broke every request.)*

### `PortLocator.java` (GPS helper)
Holds the coordinates of the six ports. `nearestPort(lat,lng)` finds the closest port (used by GPS auto-fill); `coordsFor(name)` returns a port's coordinates (used by the map).

---

## 6. Cloud REST API (`/server`)

A lightweight **Node.js + Express** service (deployable to Render.com) that serves schedules and stores bookings. Data mirrors the app's seed boats.

**Endpoints**
- `GET /` — health check
- `GET /api/boats?from=X&to=Y` — boats on a route (case-insensitive), or all boats
- `POST /api/bookings` — create a booking (returns a `SMB-YYYY-####` reference)
- `GET /api/bookings?email=X` — a user's bookings, newest first

**Deploy:** Root Directory `server`, Build `npm install`, Start `npm start` (see `server/README.md`).

> Note: Render's free tier sleeps after inactivity, so the first search after idle can take ~30–50s to wake the server.

---

## 7. Permissions & dependencies

**AndroidManifest permissions**
- `INTERNET`, `ACCESS_NETWORK_STATE` — REST API + weather + map tiles
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` — GPS nearest port

**Registered activities:** MainActivity, LoginActivity, RegisterActivity, HomeActivity, activity_search_boat, activity_available, PaymentActivity, activity_ticket, activity_my_booking, WeatherActivity, MapActivity.

**Dependencies:** AndroidX (AppCompat, ConstraintLayout, CardView), Material Components, **Volley 1.2.1** (HTTP). Build via Gradle version catalog (`gradle/libs.versions.toml`) + `app/build.gradle.kts`.

---

## 8. Source files (implemented)

**Java**
`DatabaseHelper.java`, `Boat.java`, `Booking.java`, `SessionManager.java`,
`MainActivity.java`, `LoginActivity.java`, `RegisterActivity.java`, `HomeActivity.java`,
`activity_search_boat.java`, `activity_available.java`, `PaymentActivity.java`,
`activity_ticket.java`, `activity_my_booking.java`, `WeatherActivity.java`, `MapActivity.java`,
`ApiClient.java`, `WeatherService.java`, `PortLocator.java`

**Layouts**
`activity_main`, `activity_login`, `activity_register`, `activity_home`,
`activity_search_boat`, `activity_available`, `activity_payment`, `activity_ticket`,
`activity_my_booking`, `activity_weather`, `activity_map`

**Server**
`server/index.js`, `server/package.json`, `server/README.md`

---

## 9. Proposal feature checklist

| # | Proposed feature | Status |
|---|---|---|
| 1 | User registration and login | ✅ SQLite-backed |
| 2 | Search of boats by destination | ✅ Cloud + offline |
| 3 | Viewing of schedules | ✅ Available screen |
| 4 | Online booking | ✅ SELECT → booking |
| 5 | Payment integration | ✅ Simulated mobile money |
| 6 | Digital ticket generation | ✅ Reference + QR, after payment |
| 7 | Booking history | ✅ My Bookings (SQLite) |
| 8 | Notifications | ⚠️ Toast confirmations (in-app) |
| 9 | Profile management | ⚠️ Not yet implemented |
| — | Weather check | ✅ Live OpenWeatherMap |
| — | Nearest-port GPS | ✅ LocationManager |
| — | Map view | ✅ OpenStreetMap/Leaflet |
| — | Cloud REST API | ✅ Node.js on Render |

Items marked ⚠️ are the remaining optional extensions.

---
*Generated for the Smart Marine Booking Application — implementation reference.*