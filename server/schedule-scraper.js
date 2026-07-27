// schedule-scraper.js
// Fetches real ferry schedules from zanzibarferries.com and returns
// boat data for the API. Falls back to curated static data if the
// site is unreachable.

const fetch = require("node-fetch");
const cheerio = require("cheerio");

const SCHEDULE_URL =
  "https://zanzibarferries.com/zanzibar-ferry-schedule-todays-departures-arrivals/";

const PRICES = { economy: 35000, business: 40000, vip: 70000, royal: 110000 };

// Static routes not available on zanzibarferries.com.
// Pemba: operated by ZanFast Ferries (~6x/week Zanzibar↔Pemba, ~2h)
// Mafia: operated by Kivukoni Ferry (irregular schedule, ~3h)
const STATIC_ROUTES = [
  { name: "ZanFast Ferry",          origin: "Zanzibar",     destination: "Pemba",         departure_time: "08:00 AM", arrival_time: "10:00 AM", price: 35000 },
  { name: "ZanFast Ferry",          origin: "Pemba",        destination: "Zanzibar",      departure_time: "08:00 AM", arrival_time: "10:00 AM", price: 35000 },
  { name: "Kivukoni Ferry",         origin: "Dar es Salaam", destination: "Mafia",        departure_time: "07:00 PM", arrival_time: "10:00 PM", price: 40000 },
  { name: "Kivukoni Ferry",         origin: "Mafia",         destination: "Dar es Salaam", departure_time: "07:00 PM", arrival_time: "10:00 PM", price: 40000 },
];

// Fallback if scraping fails entirely
const FALLBACK_DAR_ZNZ = [
  { name: "Kilimanjaro Fast Ferry", origin: "Dar es Salaam", destination: "Zanzibar", departure_time: "07:00 AM", arrival_time: "08:45 AM", price: 35000 },
  { name: "Kilimanjaro II",         origin: "Dar es Salaam", destination: "Zanzibar", departure_time: "09:30 AM", arrival_time: "11:15 AM", price: 35000 },
  { name: "Kilimanjaro III",        origin: "Dar es Salaam", destination: "Zanzibar", departure_time: "12:30 PM", arrival_time: "02:15 PM", price: 35000 },
  { name: "Kilimanjaro IV",         origin: "Dar es Salaam", destination: "Zanzibar", departure_time: "03:45 PM", arrival_time: "05:30 PM", price: 35000 },
  { name: "Kilimanjaro Fast Ferry", origin: "Zanzibar",      destination: "Dar es Salaam", departure_time: "07:00 AM", arrival_time: "08:45 AM", price: 35000 },
  { name: "Kilimanjaro II",         origin: "Zanzibar",      destination: "Dar es Salaam", departure_time: "09:30 AM", arrival_time: "11:15 AM", price: 35000 },
  { name: "Kilimanjaro III",        origin: "Zanzibar",      destination: "Dar es Salaam", departure_time: "12:30 PM", arrival_time: "02:15 PM", price: 35000 },
  { name: "Kilimanjaro IV",         origin: "Zanzibar",      destination: "Dar es Salaam", departure_time: "03:30 PM", arrival_time: "05:15 PM", price: 35000 },
];

async function scrapeSchedules() {
  try {
    console.log("[Scraper] Fetching schedules from zanzibarferries.com...");
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);

    const response = await fetch(SCHEDULE_URL, {
      headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" },
      signal: controller.signal,
    });
    clearTimeout(timer);

    if (!response.ok) throw new Error(`HTTP ${response.status}`);

    const html = await response.text();
    const $ = cheerio.load(html);
    const boats = [];
    let id = 1;

    // Split HTML into chunks around each <table> to determine direction.
    // The page has two main schedule sections:
    //   1. "Zanzibar to Dar es Salaam" (first table)
    //   2. "Dar es Salaam to Zanzibar" (second table)
    const tables = $("table");
    const fullHtml = html;

    tables.each((_ti, table) => {
      const $table = $(table);
      const tableMarkup = $.html($table);
      const tablePos = fullHtml.indexOf(tableMarkup);

      // Look at ~3000 chars before this table for direction headings
      let origin = "Dar es Salaam";
      let destination = "Zanzibar";

      if (tablePos > 0) {
        const before = fullHtml.substring(Math.max(0, tablePos - 3000), tablePos).toLowerCase();
        // Check last heading-like text before the table
        const hasZanzibarToDar = /zanzibar[\s\S]{0,30}(to|&|→)[\s\S]{0,30}dar/i.test(before);
        const hasDarToZanzibar = /dar[\s\S]{0,30}(es\s*salaam\s*)?(to|&|→)[\s\S]{0,30}zanzibar/i.test(before);

        if (hasZanzibarToDar && !hasDarToZanzibar) {
          origin = "Zanzibar";
          destination = "Dar es Salaam";
        } else if (hasDarToZanzibar && hasZanzibarToDar) {
          // Both found — use whichever appears LATER (closer to the table)
          const zdLast = before.lastIndexOf("zanzibar");
          const ddLast = before.lastIndexOf("dar es salaam") !== -1
            ? before.lastIndexOf("dar es salaam")
            : before.lastIndexOf("dar");
          if (zdLast > ddLast) {
            origin = "Zanzibar";
            destination = "Dar es Salaam";
          }
        }
        // If only hasDarToZanzibar or neither, keep default (Dar→Zanzibar)
      }

      // Parse rows
      $table.find("tr").each((_ri, row) => {
        const cells = $(row).find("td");
        if (cells.length < 2) return;

        const departureTime = $(cells[0]).text().trim();
        const ferryName = $(cells[1]).text().trim();
        if (!departureTime || !ferryName) return;
        if (/duration/i.test(departureTime)) return;

        const timeMatch = departureTime.match(/(\d{1,2}:\d{2})\s*(AM|PM)?/i);
        if (!timeMatch) return;

        let depTime = timeMatch[1];
        if (timeMatch[2]) depTime += " " + timeMatch[2].toUpperCase();

        boats.push({
          id: id++,
          name: ferryName,
          origin,
          destination,
          departure_time: depTime,
          arrival_time: estimateArrival(depTime),
          price: PRICES.economy,
        });
      });
    });

    if (boats.length > 0) {
      console.log(`[Scraper] Scraped ${boats.length} Dar<->Zanzibar schedules`);
    } else {
      console.log("[Scraper] No schedules parsed, using fallback");
      boats.push(...FALLBACK_DAR_ZNZ);
    }

    // Append static routes (Mafia, Pemba)
    STATIC_ROUTES.forEach((r) => boats.push({ id: id++, ...r }));

    console.log(`[Scraper] Total boats: ${boats.length}`);
    return boats;
  } catch (error) {
    console.error("[Scraper] Scrape failed:", error.message);
    console.log("[Scraper] Using full fallback data");
    const all = [...FALLBACK_DAR_ZNZ];
    let id = all.length + 1;
    STATIC_ROUTES.forEach((r) => all.push({ id: id++, ...r }));
    return all;
  }
}

function estimateArrival(departureTime) {
  try {
    const match = departureTime.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);
    if (!match) return departureTime;

    let hours = parseInt(match[1]);
    const minutes = parseInt(match[2]);
    const ampm = match[3].toUpperCase();

    if (ampm === "PM" && hours !== 12) hours += 12;
    if (ampm === "AM" && hours === 12) hours = 0;

    let totalMinutes = hours * 60 + minutes + 105;
    const arrH = Math.floor(totalMinutes / 60) % 24;
    const arrM = totalMinutes % 60;

    const h12 = arrH === 0 ? 12 : arrH > 12 ? arrH - 12 : arrH;
    const ap = arrH >= 12 ? "PM" : "AM";
    return `${String(h12).padStart(2, "0")}:${String(arrM).padStart(2, "0")} ${ap}`;
  } catch {
    return departureTime;
  }
}

module.exports = { scrapeSchedules };
