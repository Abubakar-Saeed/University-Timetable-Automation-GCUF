<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
  <%@ page import="com.timetablexpert.model.DashboardModel" %>
  <%@ page import="jakarta.servlet.http.HttpSession" %>

  <%@ page import="com.timetablexpert.model.User" %>



    <!DOCTYPE html>
    <html>

    <head>
      <meta charset="ISO-8859-1">
      <title>Timetable Xpert</title>

    <link rel="shortcut icon" href="resources/icon.png" type="image/x-icon">

      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/program.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/session.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/semester.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/course.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/teacher.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/generate.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/allocate.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/room.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/print.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">


    </head>

    <style>

    #login-btn {
    position:relative;

      min-width:70px;
      background-color: #1e3a8a; /* Match your blue theme */
      color: white;
      top:15px;
      padding: 8px 12px;
      border: none;
      border-radius: 5px;
      font-size: 12px;
      cursor: pointer;
    }

    #login-btn:hover {

       background-color:#287bff ; /* Change background to white on hover */
          transform: translateY(-3px); /* Slight lift effect */
          box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2); /* Enhance shadow on hover */
    }

    @media (max-width: 480px){



    #login-btn {
    position:relative;

      min-width:40px;

      top:15px;
      left:27px;

    }

    }
    </style>

    <body>

      <div class="container">


        <!-- Navigation Menu -->



        <div class="navigation">
          <ul>
            <div id= "logoImage">
            <li >

<a href="index.jsp" id="logo">
                        <img src="resources\Timetable Xpert logo.png" alt="Logo" id="logo_image">
                      </a>

            </li>
             </div>
            <li id="Dashboard">
              <a href="#">
                <span class="icon"><ion-icon name="home-outline"></ion-icon></span>
                <span class="title">Dashboard</span>
              </a>
            </li>
            <li id="Programs">
              <a href="#">
                <span class="icon"><ion-icon name="school-outline"></ion-icon></span>
                <span class="title">Programs</span>
              </a>
            </li>
            <li id="Sessions">
              <a href="#">
                <span class="icon"><ion-icon name="calendar-outline"></ion-icon></span>
                <span class="title">Sessions</span>
              </a>
            </li>
            <li id="Semesters">
              <a href="#">
                <span class="icon"><ion-icon name="map-outline"></ion-icon></span>
                <span class="title">Semesters</span>
              </a>
            </li>
            <li id="Courses">
              <a href="#">
                <span class="icon"><ion-icon name="book-outline"></ion-icon></span>
                <span class="title">Courses</span>
              </a>
            </li>
            <li id="Teachers">
              <a href="#">
                <span class="icon"><ion-icon name="people-outline"></ion-icon></span>
                <span class="title">Teachers</span>
              </a>
            </li>
            <li id="Rooms & Labs">
              <a href="#">
                <span class="icon"><ion-icon name="business-outline"></ion-icon></span>
                <span class="title">Rooms & Labs</span>
              </a>
            </li>
            <li id="Allocate Course">
              <a href="#">
                <span class="icon"><ion-icon name="bookmarks-outline"></ion-icon></span>
                <span class="title">Allocate Course</span>
              </a>
            </li>
            <li id="Generate Timetable">
              <a href="#">
                <span class="icon"><ion-icon name="calendar-number-outline"></ion-icon></span>
                <span class="title">Generate Timetable</span>
              </a>
            </li>
            <li id="Print Timetable">
              <a href="#">
                <span class="icon"><ion-icon name="print-outline"></ion-icon></span>
                <span class="title">Print Timetable</span>
              </a>
            </li>
            <li id="Admin Section">
              <a id="#">
                <span class="icon"><ion-icon name="people-circle-outline"></ion-icon></span>
                <span class="title">Manage Admins</span>
              </a>
            </li>

          </ul>
        </div>


         <!-- Top Bar -->
         <div class="topbar">
           <div class="toggle">
             <ion-icon name="menu-outline"></ion-icon>
           </div>

           <div class="search">
             <label for="search-input">
               <input type="text" id="search-input" placeholder="Search here">
               <ion-icon name="search-outline"></ion-icon>
             </label>
           </div>

      <%
          // Check if the user is authenticated
          String username = null;
          String imageBase64 = null;
          String imageSrc = "resources/user.jpg"; // Default image

          // Validate the session and retrieve attributes
          if (session != null) {
              username = (String) session.getAttribute("username");
              imageBase64 = (String) session.getAttribute("image");
          }

          // Update image source if base64 string is present
          if (imageBase64 != null) {
              imageSrc = "data:image/png;base64," + imageBase64;
          }
      %>

      <div class="action">
          <% if (imageBase64 == null) { %>
              <!-- Show login button if the user is not authenticated -->
             <button id="login-btn" onclick="window.location.href='login.jsp'">Login</button>

              <div class="profile" style = "visibility:hidden;">

              </div>
          <% } else { %>
              <!-- Show profile image if authenticated -->
              <div class="profile" style = "visibility:visible;">
                  <img src="<%= imageSrc %>" id="profile-image" alt="User Profile" />
              </div>
          <% } %>

              <div class="menu">
                  <%
                    // Show username if available
                    if (username != null) {
                  %>
                      <h3 id="admin-name"><%= username %></h3>
                      <ul>
                        <li id="logout">
                          <span class="icon"><ion-icon name="log-in"></ion-icon></span>
                          <a href="">Sign out</a>
                        </li>
                      </ul>
                  <%
                    }
                  %>
             </div>
           </div>
         </div>

        <!-- Main Dashboard -->
        <div class="main" id="dashboard-section" style="display:block;">

          <jsp:include page="/WEB-INF/view/dashboard.html" />

        </div>

        <!-- Program Section -->
        <div class="main-section" id="programs-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/programs.html" />
        </div>


        <!-- Session section -->
        <div class="main-section" id="sessions-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/sessions.html" />

        </div>


        <!-- semester section -->
        <div class="main-section" id="semesters-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/semesters.html" />
        </div>


        <!-- course section -->
        <div class="main-section" id="courses-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/courses.html" />

        </div>


        <!-- teacher section  -->
        <div class="main-section" id="teachers-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/teachers.html" />

        </div>


        <!-- room section -->
        <div class="main-section" id="rooms-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/room.html" />

        </div>


        <!-- allocate section -->
        <div class="main-section" id="allocate-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/allocate.html" />

        </div>

        <!-- generate section -->
        <div class="main-section" id="generate-section" style="display:none;">
          <jsp:include page="/WEB-INF/view/generate.html" />

        </div>


        <!-- print section -->
        <div class="main-section" id="print-section" style="display:none;">

                    <jsp:include page="/WEB-INF/view/print.jsp" />

        </div>

            <!-- Manage Admin section -->
                <div class="main-section" id="admin-section" style="display:none;">

                            <jsp:include page="/WEB-INF/view/manageAdmin.html" />

                </div>



      </div>


      <script type="module" src="https://unpkg.com/ionicons@7.1.0/dist/ionicons/ionicons.esm.js"></script>
      <script nomodule src="https://unpkg.com/ionicons@7.1.0/dist/ionicons/ionicons.js"></script>
      <!-- Charts integrate -->

       <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
              <script  src="js/program.js"></script>

       <script  src="js/index.js"></script>

       <script  src="js/dashboard.js"></script>
       <script  src="js/session.js"></script>
       <script src="js/course.js"></script>
       <script  src="js/teacher.js"></script>
       <script  src="js/room.js"></script>
       <script src="js/generate.js"></script>
       <script  src="js/allocate.js"></script>
       <script  src="js/print.js"></script>
       <script  src="js/semester.js"></script>
       <script  src="js/main.js"></script>
       <script  src="js/admin.js"></script>













    </body>

    </html>