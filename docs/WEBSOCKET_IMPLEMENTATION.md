# Java WebSocket Implementation — Home Inventory Project

This document describes the WebSocket implementation used for real-time notifications in the Home Inventory desktop app: which files are involved, how it works, the end-to-end flow, how it fits the “simple core Java” tech stack, why two separate servers run on two ports, and which frameworks can serve both HTTP and WebSocket on a single server.

---

## 1. Overview

The project uses **WebSockets** to push **activity-update notifications** from the backend to the JavaFX desktop client. When a user performs an action that is logged (e.g. add/edit item, user, or permission), the backend writes to the database and then **broadcasts** a simple message (`"activity_update"`) over WebSocket. Connected clients receive this and refresh the Dashboard KPIs and “Recent Updates” list without a manual refresh.

- **Backend**: Java 17, `com.sun.net.httpserver.HttpServer` for REST API (port **5000**), **Java-WebSocket** library for WebSocket server (port **5001**).
- **Frontend**: JavaFX desktop app using the same **Java-WebSocket** library as a WebSocket **client** that connects to `ws://localhost:5001/notifications`.

---

## 2. Files Involved

### Backend (server side)

| File | Role |
|------|------|
| `src/main/java/com/desktopapp/backend/Server.java` | Entry point. Creates HTTP server on port 5000, mounts REST routes, and **starts** the WebSocket server on port 5001. On shutdown, stops both. |
| `src/main/java/com/desktopapp/backend/websocket/NotificationWebSocketServer.java` | WebSocket **server**: extends `WebSocketServer` (Java-WebSocket), listens on 5001, accepts connections, and broadcasts messages to all connected clients. Singleton: `startServer(port)` / `stopServer()` / `broadcastNotification(message)`. |
| `src/main/java/com/desktopapp/backend/controllers/ActivityLogController.java` | After successfully inserting an activity log (POST `/api/activity/add`), calls `NotificationWebSocketServer.broadcastNotification("activity_update")` so all clients get a real-time signal. |
| `src/main/java/com/desktopapp/backend/routes/ActivityRoutes.java` | Registers `/api/activity/` (GET recent) and `/api/activity/add` (POST log) on the **HTTP** server. |

### Frontend (client side)

| File | Role |
|------|------|
| `src/main/java/com/desktopapp/frontend/constants/AppConstants.java` | Defines `WS_NOTIFICATIONS_URL = "ws://localhost:5001/notifications"` (and `API_BASE_URL` for HTTP on 5000). |
| `src/main/java/com/desktopapp/frontend/services/NotificationWebSocketClient.java` | WebSocket **client**: extends `WebSocketClient` (Java-WebSocket), connects to the URL above, and forwards received messages to a `Listener#onNotificationReceived(String)`. |
| `src/main/java/com/desktopapp/frontend/views/DashboardKpiView.java` | Creates the WebSocket client with `AppConstants.WS_NOTIFICATIONS_URL`, connects in a daemon thread, and on `onNotificationReceived` calls `refresh()` (KPIs + recent updates). Handles `ensureWebSocketConnected()` when the Dashboard tab is selected and `dispose()` to close the client. |
| `src/main/java/com/desktopapp/frontend/views/DashboardView.java` | When the Dashboard tab is selected, calls `dashboardKpiView.ensureWebSocketConnected()` so the WebSocket is (re)connected when the user switches to the Dashboard. |

### Build / dependencies

| File | Role |
|------|------|
| `pom.xml` | Declares **Java-WebSocket** dependency: `org.java-websocket:Java-WebSocket:1.5.6`. Used by both backend (server) and frontend (client). |

---

## 3. How It Works

### Backend (WebSocket server)

1. **Start**: `Server.start()` calls `NotificationWebSocketServer.startServer(5001)`. A single `NotificationWebSocketServer` instance is created, bound to `InetSocketAddress(5001)`, and started in its own thread(s) by the library.
2. **Connections**: Desktop clients connect to `ws://localhost:5001/notifications`. The path `/notifications` is not routed explicitly; Java-WebSocket accepts any path on that port. Each connection is tracked by the library.
3. **Broadcast**: When `broadcastNotification("activity_update")` is called (from `ActivityLogController` after logging activity), the server sends the string `"activity_update"` to **all** currently connected WebSocket clients.
4. **Lifecycle**: `Server.stop()` calls `NotificationWebSocketServer.stopServer()`, which stops the WebSocket server and sets the singleton to `null`.

### Frontend (WebSocket client)

1. **Init**: `DashboardKpiView` builds a `NotificationWebSocketClient(URI(WS_NOTIFICATIONS_URL), message -> Platform.runLater(this::refresh))` and calls `connect()` in a daemon thread (so it doesn’t block the JavaFX thread).
2. **On message**: When the server sends `"activity_update"`, `onMessage` runs and the listener invokes `Platform.runLater(this::refresh)`, so the UI refreshes KPIs and recent updates on the JavaFX thread.
3. **Reconnect**: When the user switches to the Dashboard tab, `ensureWebSocketConnected()` runs; if the client is null or not open, it (re)initializes and connects again.
4. **Cleanup**: `dispose()` closes the client and sets it to null.

### Trigger (when does a notification get sent?)

- **Only** when the backend handles **POST `/api/activity/add`** (log activity). After a successful insert, it calls `NotificationWebSocketServer.broadcastNotification("activity_update")`. Any other API (e.g. GET activity, auth, users, items) does **not** send a WebSocket message by itself.

---

## 4. Complete Flow (sequence)

```
1. User opens Dashboard in JavaFX app
   → DashboardKpiView.initNotificationWebSocket()
   → NotificationWebSocketClient connects to ws://localhost:5001/notifications

2. User (or another user) performs an action that logs activity
   (e.g. add item, edit user, change permission)
   → Frontend calls POST /api/activity/add (HTTP, port 5000)

3. Backend: ActivityLogController.log
   → Validates body, inserts row into activity_log
   → NotificationWebSocketServer.broadcastNotification("activity_update")
   → Sends "activity_update" to all WebSocket clients on port 5001

4. Frontend: NotificationWebSocketClient.onMessage("activity_update")
   → Listener runs: Platform.runLater(dashboardKpiView::refresh)
   → refresh() → loadKpis() + loadRecentUpdates()
   → loadRecentUpdates() calls GET /api/activity/?for_role=... (HTTP, port 5000)
   → UI updates: KPIs and "Recent Updates" list

5. User switches away from Dashboard and back
   → Tab listener calls dashboardKpiView.ensureWebSocketConnected()
   → If client is closed, reconnects to ws://localhost:5001/notifications
```

So: **one HTTP server (5000)** for all REST (including activity log and fetch), and **one WebSocket server (5001)** only for pushing the `"activity_update"` signal. The actual data for the dashboard still comes from HTTP GETs.

---

## 5. How This Fits the “Simple Core Java” Tech Stack

- **No Spring, no Jakarta EE**: The REST API uses only the JDK’s `com.sun.net.httpserver.HttpServer`. No application server.
- **Minimal dependency for WebSocket**: A single library, **Java-WebSocket**, is used for both the server and the client. No extra framework.
- **Same language everywhere**: Backend and frontend are both Java (with JavaFX for UI), so one language and one build (Maven).
- **Straightforward model**: One broadcast channel for one purpose (activity refresh). No custom protocol; just a string message that triggers a refresh.

The only “non-core” parts are:
- **Java-WebSocket** (third-party library for WebSocket).
- **com.sun.net.httpserver** (JDK built-in but technically “internal” API; in practice stable for this use).

So the implementation stays close to “core Java + one WebSocket library + JDK HTTP server.”

---

## 6. Why Two Separate Servers on Two Ports?

**Short answer**: Because the built-in **`com.sun.net.httpserver.HttpServer`** only speaks HTTP. It does **not** implement the WebSocket protocol (HTTP upgrade, framing, etc.). So you cannot serve WebSocket on the same server instance.

- **Port 5000**: `HttpServer` — REST only (GET/POST to `/api/...`).
- **Port 5001**: Java-WebSocket’s `WebSocketServer` — WebSocket only.

So two ports are required with the **current** stack: one for HTTP, one for WebSocket. There is no way to add WebSocket to `HttpServer` without reimplementing the protocol yourself; the JDK does not provide a WebSocket API.

---

## 7. Frameworks / Libraries That Support HTTP and WebSocket on One Server

If you want **one process and one port** for both REST and WebSocket, you need a server that supports:
- HTTP (for your existing REST API), and  
- WebSocket (e.g. upgrade from HTTP to WebSocket on a path like `/notifications`).

Below are common options in the Java ecosystem. Migrating would mean replacing `HttpServer` and possibly the current WebSocket server with the chosen framework’s HTTP + WebSocket support.

### 7.1 Jetty (Eclipse Jetty)

- **HTTP**: Servlet API or Jetty’s low-level `Handler` API.
- **WebSocket**: Native support via `org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer` and `@WebSocket` or programmatic endpoints.
- **Single port**: Yes. One Jetty `Server` with a single `ServerConnector` can serve both HTTP and WebSocket (WebSocket is an HTTP upgrade on the same port).
- **Fit**: Good for “simple Java” if you use Jetty’s embedded API; no need for a full Jakarta EE app server.

### 7.2 Spring Boot + Spring WebSocket

- **HTTP**: Spring MVC (REST controllers).
- **WebSocket**: `spring-websocket` (with STOMP or raw WebSocket).
- **Single port**: Yes. One embedded Tomcat/Jetty; WebSocket upgrade on the same port.
- **Fit**: Adds Spring’s full stack; moves away from “minimal core Java” but is very common and well documented.

### 7.3 Undertow (Red Hat / WildFly)

- **HTTP**: Servlet or Undertow’s `HttpHandler` API.
- **WebSocket**: Built-in `WebSocketConnectionCallback` and upgrade handling.
- **Single port**: Yes. One Undertow server, one listener; HTTP and WebSocket on the same port.
- **Fit**: Lightweight and embeddable; can feel closer to “core Java” than Spring if you use handlers rather than servlets.

### 7.4 Vert.x

- **HTTP**: `HttpServer`, routes, etc.
- **WebSocket**: `HttpServer#webSocketHandler()` for upgrade on a path.
- **Single port**: Yes. One `HttpServer` can handle both HTTP and WebSocket on the same port.
- **Fit**: Reactive/async model; different programming model (callbacks/promises) but single dependency and one port.

### 7.5 Summary

| Technology              | HTTP + WebSocket on one port? | Notes                                      |
|-------------------------|--------------------------------|--------------------------------------------|
| JDK HttpServer + Java-WebSocket (current) | No (two ports)          | Current setup.                             |
| Eclipse Jetty           | Yes                            | Embedded, one connector, one port.        |
| Spring Boot + WebSocket | Yes                            | Full Spring stack.                         |
| Undertow                | Yes                            | Lightweight, embeddable.                   |
| Vert.x                  | Yes                            | Single server, reactive style.             |

If the goal is to keep the project “simple core Java” but move to a single port, **Jetty** or **Undertow** are the most direct replacements: embed one server, one port, and register both your existing HTTP handlers and a WebSocket endpoint (e.g. `/notifications`) in the same process.

---

## 8. Quick Reference

- **HTTP API base**: `http://localhost:5000/api` (e.g. `/api/activity/add`, `/api/activity/`).
- **WebSocket URL**: `ws://localhost:5001/notifications`.
- **Message**: Server sends `"activity_update"`; client refreshes Dashboard KPIs and recent updates.
- **Backend start**: `mvn compile exec:java -Dexec.mainClass="com.desktopapp.backend.Server"`.
- **Library**: `org.java-websocket:Java-WebSocket:1.5.6` (server and client).

This document reflects the implementation as of the current codebase.
