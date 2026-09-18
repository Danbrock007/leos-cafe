# Leo's Cafe · Ordering and operations app

A mobile-first installable web app (PWA) with a Node.js server. Customer ordering, cafe operations, rider dispatch and cash reporting work on one shared server. The supplied logo, printed menu and deals images are included as references. Item prices are editable: verify them against the original print before taking live orders.

## Run locally

Requires Node.js 20+; no npm packages or database setup are needed.

**Windows PowerShell** (set your own admin password before first launch):

```powershell
$env:ADMIN_USER="khurram.saeed"
$env:ADMIN_PHONE="03225302070"
$env:ADMIN_PASSWORD=Read-Host "Initial admin password"
$env:PORT="3000"
node server.js
```

For the requested initial account, enter the password supplied in the conversation at that prompt. Do not commit it to GitHub. Once created, the admin record stays in `data/store.json` and future restarts do not need `ADMIN_PASSWORD`. On a private computer visit `http://localhost:3000`. Run `npm test` to verify order, rider and reward flows. `npm run check` checks JavaScript syntax.

**Real phone OTP:** set `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, and `TWILIO_FROM` in the server environment. In development, the six-digit OTP is printed to the server terminal. In production (`NODE_ENV=production`), registration and reset reject requests unless SMS is configured. The admin reset code goes to 03225302070 when that is the admin's registered phone. Set `COOKIE_SECURE=true` with HTTPS.

## Put it on GitHub

Create a private repository named `leos-cafe`, extract this ZIP, then from the extracted folder:

```bash
git init
git add .
git commit -m "Initial Leo's Cafe app"
git branch -M main
git remote add origin https://github.com/Danbrock007/leos-cafe.git
git push -u origin main
```

`data/store.json` and `.env` are ignored. Back up `data/store.json` securely: it contains customers, sessions, orders, and password hashes. Do not put it in GitHub. For deployment use persistent server storage, HTTPS, a private admin credential, and a configured SMS account. The included JSON storage is suited to a single Node process, not multiple replicas or heavy concurrent traffic.

## Roles and workflow

- **Customer:** register using phone OTP, set address/location, browse menu/deals, order for cash on delivery, view status and estimated countdown, see rider location after dispatch, view history and rewards. Every tenth delivered order adds 50 points. Redeem 1,000 points for one available pizza; delivery fee still applies.
- **Admin:** manage prices/availability/deals, opening hours, contact and map coordinates, confirm and progress orders, approve rider requests, assign deliveries, view customer profiles and order history, filter sales by UTC order date, export CSV.
- **Rider:** phone register and await admin approval, accept ready orders, share foreground location with permission, mark delivered. The customer sees a map link and timestamp while an order is out for delivery.

## Platform and operating limits

This package is a working **PWA and server**, not a native Android APK. Install it on a phone from an HTTPS deployment via the browser's “Add to Home Screen.” The service worker caches the screen shell; ordering always needs a connection. Rider geolocation uses browser `watchPosition` and requires an open, active rider screen. Browsers may stop location updates in the background; this version does **not** provide continuous WhatsApp-style background tracking, native push notifications, map route/ETA calculations, card payment, or SMS without Twilio credentials. An Android/iOS release with reliable background tracking and push requires native mobile clients, device permission flows, production hosting and push/SMS infrastructure. The countdown is the configured estimated delivery time, not traffic-based routing. Set the cafe map coordinates in Settings; the displayed delivery radius is informational pending address geocoding. Date filters use UTC order creation dates; timestamps display in Pakistan time.

The initial printed menu contains small text. Some sizes and add-ons are ambiguous in the image, so only clearly legible base prices are seeded. Verify each product and deal in the admin menu before launch.
