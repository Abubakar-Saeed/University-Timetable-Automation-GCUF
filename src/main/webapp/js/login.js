const generate_btn = document.getElementById("generate_btn");
const regenerate_btn = document.getElementById("regenerate_btn");
const reset_btn = document.getElementById("reset_btn");
const section_admin = document.getElementById("admin-section");
const admin_btn = document.getElementById("Admin Section");
const profileImage = document.getElementById("profile-image");
const delBtns = document.getElementsByClassName("delete_btn");
const adBtns = document.getElementsByClassName("add_btn");
const upBtns = document.getElementsByClassName("update_btn");
const sign_btn = document.getElementById("loginBtn");

let role = -1;

async function authenticateUser(event) {
    event.preventDefault();

    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    if (!username || !password) {
        alert("Please fill in both fields.");
        return;
    }

    try {
        const response = await fetch("login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
        });

        if (response.ok) {
            const data = await response.json();


            if (data.success) {


                role = data.isSuperAdmin ? 1 : 2;
                 localStorage.setItem("userRole", role);

                window.location.href = "main.jsp";

            } else {
                alert(data.message || "Login failed, please try again.");
            }


        } else {
            const error = await response.json();
            alert("Login failed: " + error.message);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Failed to connect to the server.");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    sign_btn.addEventListener('click', authenticateUser);
});


