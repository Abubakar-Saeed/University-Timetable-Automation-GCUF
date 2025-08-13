# 📅 Timetable Xpert – University Timetable Automation (GCUF)

A **Java-based** automated timetable generation system for **Government College University Faisalabad (GCUF)**.  
The system generates **conflict-free timetables** for all departments, ensuring **no clashes** between rooms, teachers, or time slots.  
It supports **multiple timetable views** for **departments**, **semesters**, **rooms**, and **teachers**, making scheduling fast, efficient, and reliable.  

---

## ✨ Features
- **Automated Timetable Generation** – No manual arrangement needed.
- **Conflict-Free Scheduling** – Ensures no clashes between classes, rooms, or instructors.
- **Multiple Perspectives** – View timetables by:
  - Department
  - Semester
  - Room
  - Teacher
- **Optimization Algorithms** – Combines **Divide & Conquer** and **Brute Force** for efficient scheduling.
- **Multi-Timetable Output** – Generates multiple possible solutions.
- **Printable Reports** – Export timetables using **Jaspersoft**.

---

## 🛠 Tech Stack
**Backend:**
- Java (Core Java, Servlets)
- MySQL (Relational Database)
- Apache Server (XAMPP)

**Frontend/UI:**
- HTML5, CSS3, JavaScript (Vanilla)
- React.js (SPA for timetable management)
- Responsive design for cross-device compatibility

**Algorithms:**
- Divide & Conquer  
- Brute Force  
- Optimization techniques for timetable arrangement

**Reporting:**
- Jaspersoft Studio

---

## 📂 System Modules
1. **Department Management** – Create and manage departments.
2. **Course & Semester Management** – Assign courses to semesters.
3. **Faculty Management** – Add teachers with subjects and availability.
4. **Room Management** – Define room capacity and types.
5. **Schedule Generation** – Automatic timetable creation.
6. **Reports & Export** – Generate and print professional timetable reports.

---

## 📸 Timetable Views
- **Department-Wise Timetable** – All semesters in a department.
- **Semester-Wise Timetable** – Class-specific scheduling.
- **Room-Wise Timetable** – Which room is occupied when.
- **Teacher-Wise Timetable** – Each teacher’s schedule.

---

## ⚙ How It Works
1. **Input Data:** Departments, courses, semesters, teachers, rooms, and availability.
2. **Algorithm Execution:**  
   - Uses **divide-and-conquer** to break scheduling by department, then semester.  
   - Uses **brute-force** for optimal arrangements.
3. **Conflict Check:** Avoids overlapping classes for teachers, rooms, or student groups.
4. **Output Generation:** Creates multiple possible timetables, which can be exported or printed.
   
## 👨‍💻 Developer
  Abubakar Saeed – BSCS (2022-2026), Government College University Faisalabad
📧 Email: abubakarsaeed915@gmail.com
