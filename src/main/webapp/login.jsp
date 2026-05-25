<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Form - Timetable Xpert</title>
    <!-- Link to Google Fonts -->
        <link rel="shortcut icon" href="resources/icon.png" type="image/x-icon">

    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@600&family=Roboto:wght@400;500&display=swap">
    <!-- Link to the external CSS file -->
     <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

</head>
<body>
    <!-- Main container for centering the login card on the screen -->
    <div class="container">
        <!-- Login card containing both the image and the form -->
        <div class="login-card">
            <!-- Left side of the card containing the decorative image -->
            <div class="card-left">
                <div class="image-cover">
                   <a href='index.jsp'>  <img src="resources/logo.png" alt="Logo" id="logo"></a>

                </div>

                <p>Organize, Optimize, Achieve</p>
            </div>
            <!-- Right side of the card containing the login form -->
            <div class="card-right">
                <!-- Title of the login form -->
                <!-- Subtitle welcoming the user -->
                <h2 class="welcome">Welcome back Admin! </h2>
                <!-- Form for user input -->
                <form action="#" method="POST">
                    <!-- Input group for username or email -->
                    <div class="input-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" required placeholder="Username">
                    </div>
                    <!-- Input group for password -->
                    <div class="input-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required placeholder="Password">
                    </div>
                    <!-- Actions section containing the Sign In button and forgot password link -->
                    <div class="actions">
                        <button type="submit" class="btn" id="loginBtn">Sign in</button>
                    </div>
                 
                  
                   
                </form>
            </div>
        </div>
    </div>

           <script  src="js/login.js"></script>



</body>
</html>