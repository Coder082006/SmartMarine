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
const { scrapeSchedules } = require("./schedule-scraper");

const app = express();
app.use(cors());
app.use(express.json());

// ---- PesaPal API 3.0 (sandbox) ----
const PESAPAL_BASE = "https://cybqa.pesapal.com/pesapalv3";

// Cache PesaPal OAuth token
let pesapalToken = null;
let pesapalTokenExpiry = 0;

// Cache registered IPN id
let cachedIpnId = null;
let cachedIpnHost = null;

async function getPesaPalToken() {
  if (pesapalToken && Date.now() < pesapalTokenExpiry) {
    return pesapalToken;
  }

  const key = process.env.PESAPAL_CONSUMER_KEY;
  const secret = process.env.PESAPAL_CONSUMER_SECRET;
  if (!key || !secret) {
    throw new Error("PesaPal keys not set on the server");
  }

  const res = await fetch(PESAPAL_BASE + "/api/Auth/RequestToken", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ consumer_key: key, consumer_secret: secret })
  });
  const data = await res.json();
  pesapalToken = data.token;
  pesapalTokenExpiry = Date.now() + 5 * 60 * 1000;
  return pesapalToken;
}

async function getRegisteredIpnId(host) {
  if (cachedIpnId && cachedIpnHost === host) return cachedIpnId;

  const token = await getPesaPalToken();
  const ipnUrl = "https://" + host + "/api/pesapal/ipn";
  const res = await fetch(PESAPAL_BASE + "/api/URLSetup/RegisterIPN", {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + token,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ url: ipnUrl, ipn_notification_type: "GET" })
  });
  const data = await res.json();
  cachedIpnId = data.ipn_id;
  cachedIpnHost = host;
  return cachedIpnId;
}

// ---- In-memory schedule data ----
let boats = [];
let lastScrape = 0;
const SCRAPE_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes

async function refreshBoats() {
  boats = await scrapeSchedules();
  lastScrape = Date.now();
}

// On startup (local server), do the initial scrape before accepting requests
const startupReady = (async () => {
  await refreshBoats();
  setInterval(refreshBoats, SCRAPE_INTERVAL_MS);
})();

// For Vercel serverless: lazily scrape if data is empty or stale
async function ensureBoats() {
  if (boats.length === 0 || Date.now() - lastScrape > SCRAPE_INTERVAL_MS) {
    await refreshBoats();
  }
}

const bookings = [];
let bookingCounter = 1000;

// ---- Routes ----

// Health check
app.get("/", (req, res) => {
  res.json({ status: "ok", service: "Smart Marine Booking API" });
});

// GET /api/boats            -> all boats
// GET /api/boats?from=X&to=Y -> boats on a route (case-insensitive)
app.get("/api/boats", async (req, res) => {
  await ensureBoats();
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

// ---- PesaPal Payment Endpoints ----

// POST /api/pesapal/pay — initiate a payment
// body: { amount, phone, email, first_name, description }
app.post("/api/pesapal/pay", async (req, res) => {
  try {
    const b = req.body || {};
    const { amount, phone, email, first_name, description } = b;

    if (!amount || !email) {
      return res.status(400).json({ error: "amount and email are required" });
    }

    const token = await getPesaPalToken();
    const ipnId = await getRegisteredIpnId(req.get("host"));
    const merchantRef = "SMB-" + Date.now() + "-" + Math.floor(Math.random() * 10000);

    const orderRes = await fetch(PESAPAL_BASE + "/api/Transactions/SubmitOrderRequest", {
      method: "POST",
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        id: merchantRef,
        currency: "TZS",
        amount: Number(amount),
        description: description || "Smart Marine Booking Payment",
        callback_url: "https://" + req.get("host") + "/api/pesapal/callback",
        notification_id: ipnId,
        billing_address: {
          email_address: email || "",
          phone_number: phone || "",
          country_code: "TZ",
          first_name: first_name || "-",
          last_name: "-"
        }
      })
    });

    const data = await orderRes.json();

    if (!data.redirect_url) {
      return res.status(502).json({ error: "PesaPal did not return a redirect_url", raw: data });
    }

    res.json({
      order_tracking_id: data.order_tracking_id,
      merchant_reference: data.merchant_reference,
      redirect_url: data.redirect_url
    });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// GET /api/pesapal/status?orderTrackingId=X — check payment status
app.get("/api/pesapal/status", async (req, res) => {
  try {
    const trackingId = req.query.orderTrackingId;
    if (!trackingId) {
      return res.status(400).json({ error: "orderTrackingId is required" });
    }

    const token = await getPesaPalToken();
    const statusRes = await fetch(
      PESAPAL_BASE + "/api/Transactions/GetTransactionStatus?orderTrackingId=" + trackingId,
      { headers: { "Authorization": "Bearer " + token } }
    );
    const data = await statusRes.json();

    let status = "PENDING";
    const code = data.status_code;
    if (code === 1) status = "COMPLETED";
    else if (code === 2) status = "FAILED";
    else if (code === 3) status = "REVERSED";
    else if (code === 0) status = "INVALID";

    res.json({
      status,
      description: data.payment_status_description || "",
      amount: data.amount || 0,
      method: data.payment_method || "",
      raw_code: code
    });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// GET /api/pesapal/ipn — IPN notification endpoint
app.get("/api/pesapal/ipn", (req, res) => {
  console.log("PesaPal IPN:", JSON.stringify(req.query));
  res.status(200).json(req.query);
});

// GET /api/pesapal/callback — redirect target after payment
app.get("/api/pesapal/callback", (req, res) => {
  res.status(200).send(
    "<!DOCTYPE html><html><head><title>Payment Complete</title></head>" +
    "<body style='text-align:center;padding-top:60px;font-family:sans-serif;'>" +
    "<h2>Payment Complete</h2>" +
    "<p>You can return to the SmartMarine app.</p>" +
    "</body></html>"
  );
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