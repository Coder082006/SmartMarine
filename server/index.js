// Smart Marine Booking - lightweight REST API
// -------------------------------------------------
// Serves boat/route schedules and stores bookings. It mirrors the data
// in the Android app's local SQLite database so the app can optionally
// fetch schedules from the cloud (proposal: "a lightweight REST API built
// with Node.js and hosted on Render.com to serve route schedules and
// booking information").
//
// Deploy: see README.md. Run locally with:  npm install && npm start

const express = require("express");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(express.json());

// ---- In-memory data (resets on restart; fine for a demo) ----
// Same seed data as the Android SQLite "boats" table.
const boats = [
  { id: 1,  name: "Azam Marine Ferry",      origin: "Dar es Salaam", destination: "Zanzibar",      departure_time: "08:00 AM", arrival_time: "10:00 AM", price: 35000 },
  { id: 2,  name: "Kilimanjaro Fast Ferry", origin: "Dar es Salaam", destination: "Zanzibar",      departure_time: "10:30 AM", arrival_time: "12:00 PM", price: 40000 },
  { id: 3,  name: "Sea Star Express",       origin: "Dar es Salaam", destination: "Zanzibar",      departure_time: "02:00 PM", arrival_time: "04:00 PM", price: 32000 },
  { id: 4,  name: "Azam Marine Ferry",      origin: "Zanzibar",      destination: "Dar es Salaam", departure_time: "09:00 AM", arrival_time: "11:00 AM", price: 35000 },
  { id: 5,  name: "Kilimanjaro Fast Ferry", origin: "Zanzibar",      destination: "Dar es Salaam", departure_time: "03:30 PM", arrival_time: "05:00 PM", price: 40000 },
  { id: 6,  name: "Sea Star Express",       origin: "Dar es Salaam", destination: "Mafia",         departure_time: "07:30 AM", arrival_time: "10:30 AM", price: 45000 },
  { id: 7,  name: "Sea Star Express",       origin: "Mafia",         destination: "Dar es Salaam", departure_time: "01:00 PM", arrival_time: "04:00 PM", price: 45000 },
  { id: 8,  name: "Azam Marine Ferry",      origin: "Zanzibar",      destination: "Pemba",         departure_time: "11:00 AM", arrival_time: "01:30 PM", price: 30000 },
  { id: 9,  name: "Azam Marine Ferry",      origin: "Pemba",         destination: "Zanzibar",      departure_time: "02:30 PM", arrival_time: "05:00 PM", price: 30000 },
  { id: 10, name: "Victoria Lake Ferry",    origin: "Mwanza",        destination: "Bukoba",        departure_time: "09:00 AM", arrival_time: "01:00 PM", price: 28000 },
  { id: 11, name: "Victoria Lake Ferry",    origin: "Bukoba",        destination: "Mwanza",        departure_time: "08:00 PM", arrival_time: "12:00 AM", price: 28000 }
];

const bookings = [];
let bookingCounter = 1000;

// ---- Routes ----

// Health check
app.get("/", (req, res) => {
  res.json({ status: "ok", service: "Smart Marine Booking API" });
});

// GET /api/boats            -> all boats
// GET /api/boats?from=X&to=Y -> boats on a route (case-insensitive)
app.get("/api/boats", (req, res) => {
  const { from, to } = req.query;
  let result = boats;

  if (from && to) {
    const f = String(from).trim().toLowerCase();
    const t = String(to).trim().toLowerCase();
    result = boats.filter(
      (b) =>
        b.origin.toLowerCase() === f && b.destination.toLowerCase() === t
    );
  }

  res.json(result);
});

// POST /api/bookings -> create a booking
// body: { user_email, passenger_name, boat_name, origin, destination,
//         travel_date, departure_time, price }
app.post("/api/bookings", (req, res) => {
  const b = req.body || {};
  if (!b.user_email || !b.boat_name) {
    return res.status(400).json({ error: "user_email and boat_name are required" });
  }

  bookingCounter += 1;
  const year = new Date().getFullYear();
  const reference = `SMB-${year}-${bookingCounter}`;

  const booking = {
    reference,
    user_email: b.user_email,
    passenger_name: b.passenger_name || "Guest",
    boat_name: b.boat_name,
    origin: b.origin || "",
    destination: b.destination || "",
    travel_date: b.travel_date || "",
    departure_time: b.departure_time || "",
    price: b.price || 0,
    status: "CONFIRMED",
    created_at: Date.now()
  };
  bookings.push(booking);

  res.status(201).json(booking);
});

// GET /api/bookings?email=X -> a user's bookings (newest first)
app.get("/api/bookings", (req, res) => {
  const email = req.query.email;
  if (!email) {
    return res.status(400).json({ error: "email query param is required" });
  }
  const result = bookings
    .filter((b) => b.user_email === email)
    .sort((a, b) => b.created_at - a.created_at);
  res.json(result);
});

// When run directly (locally, or on a normal server like Render) we start a
// long-running HTTP server. On Vercel the platform imports this file as a
// serverless function instead, so we DON'T call listen there — we just export
// the Express app (Vercel treats it as the request handler).
const PORT = process.env.PORT || 3000;
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`Smart Marine Booking API listening on port ${PORT}`);
  });
}

module.exports = app;