# UI Redesign: Attractive Pages, FCFS, Stats and the Past-Date Rule

This improvement was made after all 14 tasks were finished. It makes the website look
modern and fixes one flaw in the booking rules.

## What changed (in simple words)

| Area | Before | After |
|------|--------|-------|
| Look and feel | Plain green bar and white boxes | Dark navy and emerald theme, photo cards, icons, rounded cards, mobile friendly |
| Facilities | 3 facilities, text only | 6 facilities (added Tennis, Basketball and Cricket Nets), each with a photo and a live "free today" count |
| Home page | Title and a list | Big stadium photo, live stats strip, facility cards, a 4-step guide and an FCFS section |
| FCFS (first come, first served) | Worked but was not visible | Explained on every booking page; the manager queue shows **#1, #2...** and the exact request time (oldest first) |
| **Past dates (the flaw)** | A past slot could still be requested | **Not possible.** Picking a past date (e.g. 1 Oct 2026 when today is 15 Oct) shows a red "Booking not possible for a past date" banner, and past slots show **Past / Not possible**. The server also refuses it, even if someone tries to force it. |
| Stats | None | New **Insights** page: bookings per day (line chart), bookings by facility, peak hours, booking status and court usage per facility |
| Demo data | Empty history | 14 days of sample booking history, so the charts show real-looking numbers |

## The past-date rule (how it works)

1. **Server rule** (`BookingService.request`): if the slot's start date and time is before *now*, it throws
   `Booking not possible: ... is in the past`. This protects the system even if the page is bypassed.
2. **Page rule** (`slots.html`): if the chosen date is before today, a red banner explains why and no Book buttons are shown.
   Slots earlier today whose start time has passed are shown as **Past**.
3. A played booking can't be cancelled either.

## Tests

| Suite | Before | After |
|-------|--------|-------|
| Unit tests (JUnit + MockMvc) | 19 | **23** (new: past slot blocked, FCFS queue order, past-date banner, stats page) |
| Selenium UI tests | 5 | **5** (unchanged, all passing; element IDs were kept the same) |

Command: `mvn clean verify -Pselenium` gives 28/28 passing.

## Charts

Charts use **Chart.js 4.4.7**, stored inside the app (`static/js`), so they work without internet.
Each chart has a "Show data table" option for accessibility.

## Photos

Photos come from Wikimedia Commons under free licences (CC BY-SA / CC0). The credits are in the
page footer ("Photo credits").

## Screenshots

See [`ss/ui-redesign/`](../ss/ui-redesign/): home, facilities, insights, past-date block, register,
booking, my bookings, sign in, manager FCFS queue and the mobile view.

## Part 2: Clear dashboards for the manager and the player

### Manager dashboard (`Manager Dashboard` in the menu)

The top row shows four clickable numbers: **requests waiting**, **bookings today**, **upcoming confirmed**
and **court usage today**. Below them are four tabs:

| Tab | What the manager can do |
|-----|-------------------------|
| **Requests** | The FCFS queue (#1 = oldest request). **Confirm** or **Reject** each request. Rejecting frees the slot. |
| **Schedule** | A grid of **facility x time** for any day (Previous / Next / Today / pick a date). Each cell shows **Free**, **Past**, or the player's name with **REQUESTED** / **CONFIRMED**. |
| **All bookings** | Every booking, with **filters** (player name or email, status, facility, date) and pages of 25. The manager can confirm, reject, or **cancel** an upcoming booking (e.g. for court maintenance). |
| **Players** | Every registered player with phone, total bookings, confirmed, cancelled and last game, plus a **View bookings** link. |

New status: **REJECTED** (the manager declined a request).

### Player dashboard (`My Dashboard` in the menu)

- A welcome card and four personal numbers: **upcoming games**, **games played**, **time on court**, **favourite sport**.
- A **"Your next game"** card with the facility photo, date, time, place and a countdown (Today / Tomorrow / In N days).
- **Upcoming** and **History** tabs. Upcoming bookings can be cancelled; past ones show **Played** and a **Book again** button.
- A short legend explaining each status.

Demo accounts: manager `manager@sports.local`, player with history `aarav@demo.local`.

### Tests after Part 2

| Suite | Count |
|-------|-------|
| Unit tests | **27** (new: reject frees slot, manager cancel, manager tabs render, player dashboard) |
| Selenium UI tests | **5** |
| **Total** | **32 / 32 passing** |

Screenshots 11-18 in [`ss/ui-redesign/`](../ss/ui-redesign/) show both dashboards.
