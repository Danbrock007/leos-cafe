# Leo's Cafe · Ordering and operations app

A mobile-first installable web app (PWA) with a Node.js server. Customer ordering, cafe operations, rider dispatch and cash reporting work on one shared server. The supplied logo, printed menu and deals images are included as references. Item prices are editable: verify them against the original print before taking live orders.

## Run locally

Requires Node.js 20+; no npm packages or database setup are needed.

**Windows PowerShell** (set your own admin password before first launch):

```powershell
$env:ADMIN_USER="khurram.saeed"
$env:ADMIN_PHONE=Read-Host "Admin recovery mobile number"
$env:ADMIN_PASSWORD=Read-Host "Initial admin password"
$env:PORT="3000"
node server.js
```

For the requested initial account, enter the password supplied in the conversation at that prompt. Do not commit it to GitHub. Once created, the admin record stays in `data/store.json` and future restarts do not need `ADMIN_PASSWORD`. On a private computer visit `http://localhost:3000`. Run `npm test` to verify order, rider and reward flows. `npm run check` checks JavaScript syntax.

**Real phone OTP:** set `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, and `TWILIO_FROM` in the server environment. In development, the six-digit OTP is printed to the server terminal. In production (`NODE_ENV=production`), registration and reset reject requests unless SMS is configured. The admin reset code goes to the number used when initializing the admin account. Set `COOKIE_SECURE=true` with HTTPS.

## Put it on GitHub

The project can be uploaded to `leos-cafe`. If starting from a fresh local checkout:

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

## Download a build from GitHub Actions

After pushing the extracted project to `main`, open your GitHub repository → **Actions** → **Build Leo's Cafe package** → latest successful run → **Artifacts** → **leos-cafe-build**. GitHub downloads an outer artifact ZIP containing `leos-cafe-build.zip`. Extract both ZIPs; run `node server.js` from the inner package after setting the initial admin environment variables above. You can also open the workflow and click **Run workflow** to build again. This build is the runnable server and PWA package; it is not an APK.

## Android debug APK for phone testing

GitHub Actions now builds `leos-cafe-android-apk` with a debug-signed `leos-cafe-debug.apk`. Download the artifact, extract it and install the APK on an Android 7.0+ phone. It is for testing, not a Play Store release.

1. On a computer connected to the **same Wi-Fi** as the Android phone, run the server using the admin setup instructions above. Windows Firewall must allow TCP port 3000 on the private LAN. Find the computer's IPv4 address with `ipconfig`.
2. In the Android app, enter `http://COMPUTER-LAN-IP:3000`, for example `http://192.168.1.10:3000`, and tap Connect. Do not enter `localhost` on the phone: that points to the phone itself.
3. Sign in as admin, or register a customer/rider using the development OTP printed in the **server terminal**. Real SMS still needs Twilio configuration. Keep the server running and the phone connected to Wi-Fi.

The APK loads the same live app from your server; it does **not** contain the Node backend or customer data. Android can share location while the rider app stays in the foreground; tracking stops when the app goes to the background. For internet-wide production use, deploy the backend to HTTPS and configure SMS, secure cookies, persistent storage and notifications. The APK allows plain HTTP only to support same-LAN testing; do not use untrusted Wi-Fi for real customer orders.

## Windows portable PC app

Download the `leos-cafe-windows-portable` artifact and extract both outer and inner ZIPs. Double-click `Start-Leos-Cafe.cmd` inside the extracted folder; it includes `node.exe`, so no separate Node installation is required. On first run, enter an admin username, recovery mobile and initial password in the prompt. Then open `http://localhost:3000` in the PC browser. Keep the black server window open while taking orders. The `data/store.json` file is created alongside the server and retains accounts/orders; back it up securely. For Android phone testing, follow the same-Wi-Fi and LAN-IP steps above and allow port 3000 on Windows Private networks when prompted.
