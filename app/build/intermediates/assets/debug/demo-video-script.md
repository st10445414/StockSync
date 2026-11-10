# StockSync Demo Video Script

**Target length:** 4-6 minutes
**Setup:** Deploy the API to a public HTTPS host, confirm its Swagger page is reachable, and record the app on a physical Android phone. Confirm Google SSO and Firebase Cloud Messaging are configured before recording.

---

## Scene 1: Introduction (15 seconds)

> "This is StockSync, an offline-first inventory and sales tracking app for small businesses in South Africa. It works without internet, syncs when connectivity returns, and includes biometric login, barcode scanning, store location tracking, multi-currency support, and bilingual localization."

---

## Scene 2: Login (20 seconds)

**On screen:** The login screen with the StockSync logo and green "Continue with Google" button.

> "The app opens with a Google single sign-on screen. I will authenticate with a real Google account, after which the API validates the Google ID token and creates the StockSync session."

**Action:** Tap "Continue with Google."

---

## Scene 3: Biometric Authentication (20 seconds)

**On screen:** The biometric prompt with a lock icon, "Use Biometric" button, and "Use PIN Instead" link.

> "Next, the app asks for biometric authentication — fingerprint or face unlock — to protect sensitive stock data. If the device has no biometrics, you can skip."

**Action:** Tap "Use Biometric" (or "Skip" if on an emulator without biometrics).

---

## Scene 4: Dashboard (30 seconds)

**On screen:** The dashboard showing Today's Sales card, Low Stock / Products / Pending tiles, Top Sellers list, sync status indicator, and a Store Location card.

> "The dashboard gives an at-a-glance overview. Today's Sales shows total revenue and transaction count. The Low Stock tile shows how many products are below threshold. The Products tile shows total inventory count. The Pending tile shows unsynced changes. Below that, Top Sellers lists the best-selling products today, and the sync indicator shows whether all data is synced or how many changes are pending. At the bottom, a Store Location card lets you view your shop's GPS coordinates."

**Action:** Tap the Low Stock tile to show it navigates to the product list. Tap back. Tap the Pending tile to show the sync status screen. Tap back. Tap the Store Location card to open the location screen, then tap back.

---

## Scene 5: Store Location (20 seconds)

**Action:** Tap the "Store Location" card on the dashboard.

**On screen:** The Store Location screen showing a location pin icon, latitude and longitude coordinates, a map placeholder, and a "Refresh Location" button.

> "The Store Location screen uses GPS to capture your shop's coordinates. This is useful for businesses with multiple branches or for delivery logistics. Tap Refresh Location to re-fetch the GPS."

**Action:** Tap "Refresh Location" to show it re-fetches coordinates. Tap back to return to the dashboard.

---

## Scene 6: Adding a Product with Barcode Scan (35 seconds)

**Action:** Tap the Products tab at the bottom. Tap the green floating + button.

**On screen:** Add Product form with photo placeholder, Name, Price, Quantity, Category, and Barcode fields. The barcode field has a camera icon.

> "To add a new product, tap the plus button. Enter the product name, price, quantity, and optionally a category. The barcode field has a camera icon — tap it to open the barcode scanner, which uses the phone's camera with ML Kit to read barcodes in real time."

**Action:** Tap the camera icon in the barcode field. The barcode scanner opens with a live camera feed. Point the camera at a barcode (or simulate a scan). The scanned barcode is automatically filled into the form. Type a product name like "Coca Cola 500ml", enter price "15", quantity "48", tap the checkmark icon. The product appears in the list.

---

## Scene 7: Product List and Search (20 seconds)

**On screen:** Product list with search bar at top, product cards showing name, price, stock count, and warning icons for low/out of stock.

> "The Products tab lists all inventory. Products below their low-stock threshold show in orange, and out-of-stock items show in red with a warning icon. Use the search bar to filter by name."

**Action:** Type a few letters in the search bar to filter the list. Clear the search.

---

## Scene 8: Quick Sale with Barcode (30 seconds)

**Action:** Tap the Sell tab at the bottom.

**On screen:** A 3-column grid of product tiles, each showing name, price, and stock count. An add-product tile with a + icon appears at the end. A camera/scan icon is in the top-right corner.

> "The Sell tab is the quick-sale grid. Each tile shows the product name, price, and current stock. Tap any product to instantly log a sale. Out-of-stock items are greyed out. The scan icon in the top-right opens the barcode scanner — scan a product's barcode to quickly find and sell it. The add-product tile at the end lets you add new inventory on the fly."

**Action:** Tap a product tile. The sale is logged immediately and the stock count decreases. Tap the scan icon in the top-right to open the barcode scanner. Tap back. Tap the add-product tile to show it navigates to the Add Product screen, then tap back.

---

## Scene 9: Settings (30 seconds)

**Action:** Tap the Settings tab at the bottom.

**On screen:** Settings screen with sections for General (Language, Currency), Notifications (three toggles), Stock (low-stock threshold input), Security (biometric lock toggle), and Account (Sign Out).

> "Settings lets you change language between English and isiZulu, switch currency between ZAR, USD, and EUR, toggle low-stock alerts, daily summaries, and sync confirmations, set the low-stock threshold, and enable biometric lock."

**Action:** Tap Language, select "isiZulu" to show the UI switches language. Tap Language again and switch back to "English." Toggle the "Low Stock Alerts" switch on. Change the low-stock threshold to "10" and tap Save.

---

## Scene 10: Sync Status (20 seconds)

**Action:** Go back to the Dashboard. Tap the sync indicator at the bottom.

**On screen:** Sync Status screen showing a green "All Synced" card or an orange "X Pending Changes" card, last sync time, and a "Sync Now" button.

> "The Sync Status screen shows whether all local changes have been pushed to the server. If there are pending changes, tap Sync Now to push them. The last sync time is displayed below."

**Action:** Tap "Sync Now." The status updates to "All Synced."

---

## Scene 11: API Demonstration (20 seconds)

**Action:** Switch to your browser showing the Swagger UI at the deployed HTTPS API address.

> "The backend is a cloud-hosted .NET 8 Web API with Swagger documentation. It exposes endpoints for authentication, products, sales, stock adjustments, dashboard data, settings, devices, and synchronisation. The API uses validated Google tokens for login, JWT tokens for application access, and PostgreSQL for production data."

**Action:** Expand one endpoint (e.g., GET /api/products) and tap "Try it out" then "Execute" to show JSON results returning.

---

## Scene 12: Push Notification (20 seconds)

**Action:** Send a test low-stock or synchronisation notification from Firebase Console or the configured server and show it arriving on the physical phone.

> "Firebase Cloud Messaging delivers real-time alerts to the device. This notification confirms that the registered device can receive a live cloud message."

---

## Scene 13: Closing (15 seconds)

> "StockSync is offline-first, bilingual, secure with biometric authentication, equipped with barcode scanning and store location tracking, and designed for small businesses that need reliable inventory management without constant internet access. Thank you for watching."
