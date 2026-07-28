# Smart Marine — PesaPal Sandbox Testing

> **Everything here is SANDBOX only.** The server calls PesaPal's demo host
> `https://cybqa.pesapal.com/pesapalv3` (see `server/index.js`). No real money
> moves. **Never enter a real mobile-money PIN while testing.**

Live server: https://smart-marine.vercel.app

---

## Quick server health checks

Initiate a payment (should return an `order_tracking_id` + `redirect_url`):

```bash
curl -s -X POST "https://smart-marine.vercel.app/api/pesapal/pay" \
  -H "Content-Type: application/json" \
  -d '{"amount":1000,"phone":"0712345678","email":"test@example.com","first_name":"Test","description":"Sandbox test"}'
```

Check a payment status (`INVALID`/`PENDING` until paid, `COMPLETED` after):

```bash
curl -s "https://smart-marine.vercel.app/api/pesapal/status?orderTrackingId=<ID_FROM_ABOVE>"
```

If you ever see `{"error":"PesaPal keys not set on the server"}`, the Vercel
environment variables `PESAPAL_CONSUMER_KEY` / `PESAPAL_CONSUMER_SECRET` did not
attach to the deployment — re-add them and redeploy.

---

## 💳 Sandbox test cards (recommended path)

All cards: **Expiry `07/28`**, **CVV `123`** (Amex CVV `1234`).

| Result | Type | Card number |
|--------|------|-------------|
| ✅ Success (no OTP) — **use this first** | Mastercard | `5200 0000 0000 0114` |
| ✅ Success (no OTP) | Visa | `4761 7390 0101 0010` |
| ✅ Success (no OTP) | Amex | `3400 0000 0003 961` |
| 🔐 Success (3-D Secure, asks for OTP) | Mastercard | `5200 0000 0000 0007` |
| 🔐 Success (3-D Secure, asks for OTP) | Visa | `4000 0000 0000 1091` |
| ❌ Failure (test a decline) | Visa | `4000 0000 0000 1018` |

Source: https://cybqa.pesapal.com/PesapalIframe/PesapalIframe3/TestPayments

---

## 📱 Sandbox mobile money (Mixx by Yas / M-Pesa, etc.)

There is **no fixed demo phone number**. Use the simulator instead — it lets you
type any number and gives back a dummy confirmation code:

1. Open https://demo.pesapal.com/mobilemoneytest
2. Enter an amount + any phone number → it generates a dummy confirmation code
   (e.g. `KA67QP551`).
3. On the app's payment page choose mobile money and paste that dummy code →
   **Complete**.

⚠️ Do **not** enter your real Mixx by Yas / M-Pesa PIN. Entering a real number on
the payment page can push a genuine USSD PIN prompt to your phone — cancel it.

The buyer's email must be sent with the order for the transaction to register in
the demo account. The app already sends this (`PaymentActivity` → `startPesaPalPayment`).

---

## Public sandbox API credentials (per country)

From https://developer.pesapal.com/api3-demo-keys.txt — Smart Marine uses **TZS**,
so the **Tanzania** merchant keys apply:

```
Tanzania Merchant
consumer_key:    ngW+UEcnDhltUc5fxPfrCD987xMh3Lx8
consumer_secret: q27RChYs5UkypdcNYKzuUw460Dg=
```

These are PesaPal's public demo keys (safe to use for testing). They are set on
the server as environment variables, not hard-coded.

---

## End-to-end device test

1. Build & run the app in Android Studio.
2. Search boat → book → **Payment** → enter a 10+ digit number → **Pay**.
3. On the PesaPal page, use the **Mastercard `5200 0000 0000 0114`** card above.
4. Expect: status polls `PENDING`/`INVALID` briefly, then `COMPLETED`, then the
   app opens the **ticket screen** with "✅ Payment successful!".

Note: the poller treats only `FAILED`/`REVERSED` as terminal; `INVALID`/`PENDING`
keep polling until timeout, so a not-yet-finalized payment isn't wrongly aborted.

---

## Going live (later)

Switch `PESAPAL_BASE` in `server/index.js` from `cybqa.pesapal.com` to the
production host (`pay.pesapal.com/v3`) and set **live** consumer keys in the
server environment. Do not go live with the demo keys above.