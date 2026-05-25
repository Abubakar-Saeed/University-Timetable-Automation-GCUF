# TimetableXpert — University Timetable Automation

This repository contains TimetableXpert, a Java web application (WAR) for automating university timetabling. This README provides setup, build, database restore, deployment, contribution, and troubleshooting guidance so you (and other contributors) can run and maintain the project.

---

## Project overview

- Artifact: `TimetableXpert` (WAR)
- Frameworks / tech: Java Servlets / JSP, Maven, MySQL, JasperReports
- Primary code location: `src/main/java/com/timetablexpert` and `src/main/webapp`

---

## Prerequisites

- Java JDK 11+ (recommended JDK 17 if available)
- Apache Maven (3.6+)
- MySQL Server (8.x recommended) or compatible
- A servlet container (Apache Tomcat 9/10 or similar)
- Optional: MySQL Workbench or phpMyAdmin for DB import

---

## Database setup (required)

The application expects a MySQL database named `time_table_automation` by default. A SQL backup file is included in the project root:

- `time_table_automation_backup.sql`

Restore steps (PowerShell / Command Prompt):

```powershell
# create database (if not present)
mysql -u root -p
# enter password when prompted (default in project: root)
CREATE DATABASE IF NOT EXISTS time_table_automation;
EXIT

# import backup into the database
mysql -u root -p time_table_automation < "E:\TimetableXpert Project\Website\TimetableXpert\time_table_automation_backup.sql"
```

If `mysql` is not in your PATH use its full path, for example:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p time_table_automation < "E:\TimetableXpert Project\Website\TimetableXpert\time_table_automation_backup.sql"
```

Notes:
- Default DB credentials the app uses (unless overridden) are `root` / `root`.
- You can override JDBC connection values using system properties at runtime: `JDBC_URL`, `JDBC_USERNAME`, `JDBC_PASSWORD`.
  - Example `JDBC_URL`: `jdbc:mysql://localhost:3306/time_table_automation`

The DB connection helper is implemented in `src/main/java/com/timetablexpert/dbconnection/DbConnection.java`.

---

## Build the project

From the project root run:

```bash
mvn clean package
```

This will produce the WAR at:

```
target/TimetableXpert-1.0-SNAPSHOT.war
```

---

## Deploying the WAR

Option A — Tomcat (recommended):

1. Copy `target/TimetableXpert-1.0-SNAPSHOT.war` to `TOMCAT_HOME/webapps/`.
2. Start (or restart) Tomcat.
3. Open the app in a browser:

```
http://localhost:8080/TimetableXpert-1.0-SNAPSHOT/
```

To deploy at the root context, rename the WAR to `ROOT.war` before copying it to `webapps`.

Option B — IDE (Eclipse/IntelliJ/VS Code):

- Use the IDE's Tomcat / application server support to deploy the `war` or the exploded webapp.

---

## Running with overridden DB properties

You can pass system properties to the JVM or Tomcat to override DB defaults. Example Tomcat `setenv` (Windows `setenv.bat` or using service configuration):

```bash
-DJDBC_URL="jdbc:mysql://dbhost:3306/time_table_automation"
-DJDBC_USERNAME="dbuser"
-DJDBC_PASSWORD="dbpass"
```

When running with `mvn tomcat7:run` or similar plugin, pass `-D` properties to Maven.

---

## Verifying the application

- After deployment, try logging in via the login page (`index.jsp` / `login.jsp`).
- If database connections fail, check Tomcat logs and the DB server status.

---

## Project structure (high-level)

- `src/main/java` — Java sources
- `src/main/webapp` — JSPs, static assets (`css`, `js`)
- `target/` — built artifacts
- `time_table_automation_backup.sql` — DB backup to restore

---

## Contribution & pushing to GitHub

If you want to push this project to the repository `https://github.com/Abubakar-Saeed/University-Timetable-Automation-GCUF`, suggested steps:

```bash
# initialize git (if not already)
git init
git add .
git commit -m "Add project files and README"
# add remote (replace if different)
git remote add origin https://github.com/Abubakar-Saeed/University-Timetable-Automation-GCUF.git
git branch -M main
git push -u origin main
```

If the repo already exists and you only want to push a new branch, use `git pull` first to avoid conflicts.

---

## Troubleshooting

- `java.sql.SQLException` on startup: verify MySQL is running and credentials match.
- `ClassNotFoundException: com.mysql.cj.jdbc.Driver`: ensure MySQL Connector/J is available (the project includes it in `pom.xml`), or check classpath if running differently.
- Ports: Tomcat default port is `8080` — ensure nothing else is blocking it.

---

## License & contacts

- Add your preferred license file (`LICENSE`) to the repo if you want to make the project public.
- For questions, contact the maintainer (add details here).

---

## Next steps I can do for you

- Add a `CONTRIBUTING.md` and `LICENSE` file.
- Create a minimal `.github/workflows/ci.yml` to build the WAR on push.
- Commit and push the README for you if you want.

If you want, I can commit the README and open a branch or push directly to the provided GitHub repo — tell me how you'd like to proceed.