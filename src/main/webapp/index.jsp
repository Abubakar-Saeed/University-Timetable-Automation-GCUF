<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Timetable Xpert</title>
    <link rel="shortcut icon" href="resources/icon.png" type="image/x-icon">
   <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">


</head>

<body>
<!-- Navigation Bar -->
<header id="navBar">
    <div class="container">
      <a href='index.jsp'>  <img src="resources/logo.png" alt="Logo" id="logo"></a>
        <div class="toggle-icon">
            <ion-icon name="menu-outline"></ion-icon>
        </div>
        <nav>
            <ul class="nav-links">
                <li><a href="#Home">Home</a></li>
                <li><a href="#features_section">Features</a></li>
                <li><a href="#about_section">About</a></li>
                <li><a href="#contact">Contact</a></li>

                <li>

                <a href='login.jsp' id="loginLink"> <button class="cta-btn">Login</button></a>

                </li>
            </ul>
        </nav>
    </div>
</header>

<!-- Hero Section -->
<div class="hero-container" id="Home">
    <div class="landing">
        <div class="hero-text">
            <h1>Timetable <br> Automation System</h1>
            <p>Managing your schedule has never been easier! Our Timetable Automation System eliminates the stress
                of organizing and planning. Whether it's for classes, labs, or room allocations, this system handles
                the heavy lifting, so you can focus on what truly matters. Simple, fast, and reliable . it's
                designed to make your life hassle-free.</p>

            <br>
           <a href="main.jsp"><button class="start_btn"  style="text-decoration:none;">Get Started  </button></a>

        </div>
        <div class="hero-img">
            <img src="resources/1.jpg" alt="Hero image">
        </div>
    </div>
</div>

<div class="why-header">
    <h2 style="margin-top: 20px;">Why Timetable Xpert</h2>
    <p class="subheading">The ultimate solution to simplify and optimize your scheduling process.</p>
</div>
<div class="why-section">
    <div class="why-container">
        <!-- Section Heading -->


        <div class="why-content">
            <!-- Left Image -->
            <div class="why-image">
                <img src="resources/5.jpg" alt="Why timetableXpert">
            </div>

            <!-- Right Features -->
            <div class="why-features">
                <div class="feature">

                    <h3>Multiple Formats</h3>
                    <p>Easily create and share schedules in various formats, tailored to meet your needs.</p>
                </div>

                <div class="feature">

                    <h3>Schedule Types</h3>
                    <p> Generate schedules for Department Wise, Teacher Wise, Lab Wise and Room Wise ready for
                        print, ensuring a seamless distribution process.</p>
                </div>

                <div class="feature">

                    <h3>Manage Admins</h3>
                    <p>Effortlessly assign and manage administrative roles to streamline scheduling operations.</p>
                </div>

            </div>
        </div>
    </div>
</div>


<div id="features_section">
    <div class="why-header">
        <h2 style="margin-top: 20px;">Features</h2>
        <p class="subheading">Empowering your scheduling experience with efficiency, flexibility, and security</p>
    </div>

    <div class="cards">
        <div class="featuresCard">
            <div class="card" data-card-id="1">
                <img src="resources/Department Wise.png" alt="Department Wise">
                <h4>Department Wise</h4>
                <p>Details and management system for all departments in a structured format.</p>
            </div>
            <div class="card" data-card-id="2">
                <img src="resources/Teacher Wise.png" alt="Teacher Wise">
                <h4>Teacher Wise</h4>
                <p>Manage and view teacher schedules and profiles with ease.</p>
            </div>
            <div class="card" data-card-id="3">
                <img src="resources/Room Wise.png" alt="Room Wise">
                <h4>Room wise</h4>
                <p>View and allocate rooms schedules for each department separately.</p>
            </div>
            <div class="card" data-card-id="4">
                <img src="resources/Lab Wise.png" alt="Lab Wise">
                <h4>Lab Wise</h4>
                <p>Organize lab schedules and resources for efficient usage.</p>
            </div>
        </div>
    </div>
</div>



<div id="about_section">

    <div class="why-header">
        <h2 style="margin-top: 20px;">About</h2>

    </div>

    <div class="about-section-side">
        <div class="about_container">

            <div class="logo">
                <img src="resources/uni.jpg" alt="University Logo" class="university-logo">
            </div>

            <div class="text">
                <p> Timetable Xpert is an innovative solution developed by Abubakar Saeed, a 5th-semester BSCS student at the <b>Institute of Arts and Sciences, Government College University Faisalabad Chiniot Campus</b>. This project is designed to solve the challenges of creating conflict-free timetables, catering to the needs of both students and faculty.
                    <br>
                    The primary goal of Timetable Xpert is to simplify and streamline the complex process of university scheduling. By focusing on key aspects such as avoiding scheduling conflicts, balancing teacher workloads, optimizing room assignments, and organizing schedules by department, it ensures an efficient, student-friendly, and teacher-friendly experience.</p>
            </div>
        </div>
    </div>
</div>

<div id="contact">
    <div class="why-header">
        <h2 style="margin-top: 20px;">Contact Us</h2>
    </div>
    <div class="contact-section-side">
        <div class="contact_container">
            <!-- Developer Picture -->
            <div class="developer-picture">
                <img src="resources/developerImg.jpg" alt="Developer Picture" class="developer-img">
            </div>

            <div id="about-text-section">
                <h5>Software Developer</h5>

              <p>
                  Hi! I'm Abubakar Saeed, the developer of Timetable Xpert. I'm passionate about solving real-world problems with technology. Through my experience in software development and research, I developed Timetable Xpert to address the complexities of academic scheduling.
                          <br>      As a Computer Science student, I believe in the power of innovation to simplify everyday challenges. Timetable Xpert is my way of using what I have learned to help students, faculty, and institutions make their academic lives more organized and efficient. If you have any questions or feedback, feel free to reach out I'm always happy to help &#128578!
              </p>

                <!-- Social Media Icons -->
                <div class="social-icons">
               <a href="https://www.linkedin.com/in/abubakarsaeed915" target="_blank">
                   <ion-icon name="logo-linkedin"></ion-icon>
               </a>
               <a href="https://github.com/Abubakar-Saeed" target="_blank">
                   <ion-icon name="logo-github"></ion-icon>
               </a>
               <a href="https://www.facebook.com/profile.php?id=100082189336577" target="_blank">
                   <ion-icon name="logo-facebook"></ion-icon>
               </a>
               <a href="https://www.instagram.com/abubakarsaeed913/" target="_blank">
                   <ion-icon name="logo-instagram"></ion-icon>
               </a>
               <a href="mailto:abubakarsaeed915@gmail.com" target="_blank">
                   <ion-icon name="mail"></ion-icon>
               </a>

                </div>
            </div>
        </div>
    </div>
</div>



<div class="foot">
    <div class="footerContainer">
        <div class="leftColumn">
            <div class="footerlogo">
                <a href='index.jsp'>  <img src="resources/Timetable Xpert logo.png" alt="Logo" id="logo"></a>
            </div>

            <div class="footerpara">
                <p>Managing your schedule has never been easier! Our Timetable Automation System eliminates the stress of organizing and planning.</p>
            </div>
        </div>

        <div class="quicklinks">
            <h3>Quick Links</h3>
            <ul>
                <li><a href="#home">Home</a></li>
                <li><a href="#features">Features</a></li>
                <li><a href="#about">About Us</a></li>
                <li><a href="#contact">Contact</a></li>
            </ul>
        </div>
    </div>
</div>

<footer>
    <div class="container1">
        <p>&#169 2024 Timetable Xpert. Developed By Abubakar Saeed &#x2665</p>
    </div>
</footer>



<script type="module" src="https://unpkg.com/ionicons@7.1.0/dist/ionicons/ionicons.esm.js"></script>
<script nomodule src="https://unpkg.com/ionicons@7.1.0/dist/ionicons/ionicons.js"></script>

            <script  src="js/index.js"></script>




</body>

</html>