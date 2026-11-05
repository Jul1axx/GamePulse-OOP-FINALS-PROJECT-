# GamePulse — Java Swing + MySQL

GamePulse is a desktop application for recording gaming sessions, enforcing personal playtime targets with in-app alerts, and reviewing playtime history and analytics.

## Included features

### User account and login
- MySQL-backed login using the existing PBKDF2 password hashing utility.
- Public registration creates ordinary `USER` accounts only.
- First-run administrator setup: when no `ADMIN` account exists, GamePulse prompts you to create one.
- The authenticated role determines which dashboard opens.

### User dashboard
- Start and end a session for one selected game at a time.
- The elapsed clock is calculated from the saved start timestamp, not by counting UI timer ticks.
- An active session is restored when the user opens GamePulse again.
- A session captures the time limit that was in effect when it started.
- Per-game personal limits (1–1,440 minutes; default 60 minutes).
- A warning dialog appears when the active session reaches its limit. The app does not forcibly close the game or stop the session.
- Session history, seven-day playtime chart, average session, most-played game, exceeded-limit count, and rule-based recommendations.

### Admin dashboard
- Create accounts and assign `USER` or `ADMIN` roles.
- Change account roles and delete accounts (deleting an account also deletes its sessions and limits).
- Add and edit games; deactivate games without deleting historical session references.
- Review all user sessions.
- Create and edit user-specific game limits.

## Requirements
- Windows 10/11 (the included launcher is a `.bat` file).
- JDK 17 or newer (the project uses Java text blocks; JDK 26 is suitable).
- XAMPP or another MySQL server.
- MySQL Connector/J. The supplied `lib/mysql-connector-j-26.10.0.jar` is already included.

## Database setup

1. Start **MySQL** from the XAMPP Control Panel.
2. Open phpMyAdmin at `http://localhost/phpmyadmin`.
3. Import `database/gamepulse_schema.sql` or run its SQL in the SQL tab.
4. The schema creates the `gamepulse` database, the account/game/session/limit tables, and a starter game catalog.
5. The application also runs `CREATE TABLE IF NOT EXISTS` and seeds missing starter games at launch. It does not create sample user accounts.

The existing `users` table must have these columns because they are used by the login code: `user_id`, `username`, `password_hash`, `password_salt`, and `role`. If your existing table uses different names or types, back it up and align it with the supplied schema before launching.

## Run in VS Code on Windows

1. Extract the ZIP to a folder such as `Finals Project (OOP)/GamePulse`.
2. Open the extracted `DATAFiles` folder in VS Code (the folder containing `Main.java`).
3. Make sure XAMPP MySQL is running and the `gamepulse` database exists.
4. Open the VS Code terminal and run:

   ```bat
   run_gamepulse.bat
   ```

The launcher compiles the Java files into `build/` and starts `Main`. The first launch prompts you to create an administrator account. After that, sign in with that account. Users can create their own `USER` accounts from the login window.

You can also compile manually:

```bat
javac -encoding UTF-8 -cp "lib\mysql-connector-j-26.10.0.jar" -d build *.java
java -cp "build;lib\mysql-connector-j-26.10.0.jar" Main
```

## Database connection configuration

Defaults are:

- URL: `jdbc:mysql://localhost:3306/gamepulse`
- Username: `root`
- Password: empty

To use different settings, define these Windows environment variables before launching:

- `GAMEPULSE_DB_URL`
- `GAMEPULSE_DB_USER`
- `GAMEPULSE_DB_PASSWORD`

Example for the current XAMPP defaults:

```bat
set GAMEPULSE_DB_USER=root
set GAMEPULSE_DB_PASSWORD=
run_gamepulse.bat
```

## Project structure

```text
DATAFiles/
├── Main.java
├── SystemLogin.java
├── UserDashboard.java
├── AdminDashboard.java
├── FirstAdminSetupDialog.java
├── RegisterDialog.java
├── User.java
├── UserDAO.java
├── Game.java
├── GameDAO.java
├── GameSession.java
├── SessionDAO.java
├── SessionLimitDAO.java
├── AnalyticsData.java
├── AnalyticsService.java
├── DatabaseInitializer.java
├── DBConnection.java
├── PasswordUtil.java
├── AppTheme.java
├── WeeklyChartPanel.java
├── UiUtil.java
├── database/gamepulse_schema.sql
└── lib/mysql-connector-j-26.10.0.jar
```

## Notes and current scope

- This is a manual desktop session tracker. It records the game selected by the user; it does not inspect Windows processes or automatically detect which game is running.
- Alerts are in-app Swing dialogs. They do not block the operating system or forcibly stop a game.
- Analytics are based on completed sessions. An active session's live duration is shown in the tracker and is stored when the user ends it.
- The recommendations are rule-based examples, not medical advice or machine-learning predictions.
- Back up your database before changing the schema or deleting accounts.
- The legacy `Data.java` file is retained from the original project but the new session workflow uses `GameSession.java` instead.
