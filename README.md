# 📅 TimetableXpert – University Timetable Automation (GCUF)

A Java-based automated timetable generation system for Government College University Faisalabad (GCUF). This repository contains the TimetableXpert web application (WAR) and supporting files.

---

## ✨ Highlights
- Automated timetable generation with conflict avoidance (rooms, teachers, student groups).
- Multiple views: department, semester, room, teacher.
- Printable/exportable reports via JasperReports.
- Tech: Java Servlets / JSP, Maven, MySQL, JasperReports.

---

## Project overview

- Artifact: `TimetableXpert` (WAR)
- Primary code: `src/main/java/com/timetablexpert` and `src/main/webapp`

---

## Prerequisites

- Java JDK 11+ (JDK 17 recommended)
- Apache Maven (3.6+)
- MySQL Server (8.x recommended)
- Apache Tomcat 9/10 or similar servlet container
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

If `mysql` is not on your PATH use its full path, for example:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p time_table_automation < "E:\TimetableXpert Project\Website\TimetableXpert\time_table_automation_backup.sql"
```

Notes:
- Default DB credentials used by the app (unless overridden) are `root` / `root`.
- Override JDBC connection values using system properties at runtime: `JDBC_URL`, `JDBC_USERNAME`, `JDBC_PASSWORD`.
  - Example: `jdbc:mysql://localhost:3306/time_table_automation`

The DB connection helper is implemented in `src/main/java/com/timetablexpert/dbconnection/DbConnection.java`.

---

## Build the project

From the project root run:

```bash
mvn clean package
```

This produces the WAR at:

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

- Use the IDE's Tomcat/application server support to deploy the WAR or exploded webapp.

---

## Verifying the application

- After deployment, try logging in via `index.jsp` / `login.jsp`.
- If DB connections fail, check Tomcat logs and MySQL status.

---

## Project structure (high-level)

- `src/main/java` — Java sources
- `src/main/webapp` — JSPs and static assets (`css`, `js`)
- `target/` — built artifacts
- `time_table_automation_backup.sql` — DB backup

---

## Features & System Modules

- Automated timetable generation with conflict resolution
- Department, Course & Semester, Faculty, Room management
- Schedule generation and report export

---

## Contribution & push guidelines

To add and push changes to the GitHub repository `https://github.com/Abubakar-Saeed/University-Timetable-Automation-GCUF`:

```bash
git add .
git commit -m "Describe your change"
git pull origin main --rebase
git push origin main
```

If the remote has unrelated history, use `git pull --allow-unrelated-histories` to merge, resolve conflicts, then commit and push.

---

## Troubleshooting

- `java.sql.SQLException`: verify MySQL credentials and that `time_table_automation` exists.
- `ClassNotFoundException: com.mysql.cj.jdbc.Driver`: ensure MySQL Connector/J is available (configured in `pom.xml`).
- Git push errors: run `git pull` to sync with remote and resolve conflicts.

---

## License & Contacts

Add a `LICENSE` file to specify the project license.

Developer: Abubakar Saeed — abubakarsaeed915@gmail.com

---

## Next actions I can take

- Create `CONTRIBUTING.md` and `LICENSE` files.
- Open a branch and push these docs for review.
- Add a GitHub Action to build the WAR on push.

If you want me to finish the merge and push the resolved README for you, say "Yes, push the merge" and I will complete the commit and push.
