# Smart Marine Booking — REST API

A lightweight Node.js + Express REST API that serves boat/route schedules and
stores bookings for the Smart Marine Booking Android app. This is the
"lightweight REST API built with Node.js and hosted on Render.com" described in
the project proposal.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET  | `/`                              | Health check |
| GET  | `/api/boats`                     | All boats/schedules |
| GET  | `/api/boats?from=Dar es Salaam&to=Zanzibar` | Boats on a route |
| POST | `/api/bookings`                  | Create a booking (JSON body) |
| GET  | `/api/bookings?email=you@mail.com` | A user's bookings |

`POST /api/bookings` body:
```json
{
  "user_email": "jesca@example.com",
  "passenger_name": "Jesca John",
  "boat_name": "Azam Marine Ferry",
  "origin": "Dar es Salaam",
  "destination": "Zanzibar",
  "travel_date": "22/5/2026",
  "departure_time": "08:00 AM",
  "price": 35000
}
```

## Run locally

```bash
cd server
npm install
npm start
# -> http://localhost:3000/api/boats
```

## Deploy to Render.com (free)

1. Push this repo to GitHub (it already is).
2. On https://render.com → **New → Web Service** → connect your GitHub repo.
3. Settings:
   - **Root Directory:** `server`
   - **Build Command:** `npm install`
   - **Start Command:** `npm start`
   - **Instance type:** Free
4. Deploy. Render gives you a URL like `https://smart-marine-api.onrender.com`.
5. Put that URL in the Android app: open `ApiClient.java` and set
   `BASE_URL` to your Render URL. Then the app can pull schedules from the cloud
   instead of only local SQLite.

> Note: data is kept in memory and resets when the service restarts (fine for a
> class demo). To make it permanent, swap the in-memory arrays for a database.