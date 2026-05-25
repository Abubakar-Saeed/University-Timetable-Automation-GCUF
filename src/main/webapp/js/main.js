// Menu Toggle
let toggle = document.querySelector('.toggle');
let topBar = document.querySelector('.topbar');
let navigation = document.querySelector('.navigation');
let dashboard_section = document.getElementById('dashboard-section');
let program_section = document.getElementById('programs-section');
let sessions_section = document.getElementById('sessions-section');
let semesters_section = document.getElementById('semesters-section');
let courses_section = document.getElementById('courses-section');
let teachers_section = document.getElementById('teachers-section');
let room_section = document.getElementById('rooms-section');
let allocate_courses_section = document.getElementById('allocate-section');
let generate_section = document.getElementById('generate-section');
let print_section = document.getElementById('print-section');
let admin_section = document.getElementById('admin-section');
let logoImage = document.getElementById('logo_image');
const delBtns = document.getElementsByClassName("delete_btn");
const adBtns = document.getElementsByClassName("add_btn");
const upBtns = document.getElementsByClassName("update_btn");
const BtnGroup = document.getElementsByClassName("button-group");

const downloadBtn = document.getElementById("download_button");
const printBtn = document.getElementById("print_button");
const admin_btn = document.getElementById("Admin Section");
const profilePic = document.querySelector(".profile");
const toggleMenu = document.querySelector(".menu");

const loginButton = document.getElementById("login-btn");

const logoutBtn = document.getElementById("logout");


 let access = -1;

document.getElementById("logoImage").addEventListener('click', () => {
    window.location.href = "index.jsp";
});

// Toggle menu
toggle.onclick = function () {

  if (logoImage.src.includes("icon.png")) {
        logoImage.src = "resources/Timetable Xpert logo.png";
    } else {
        logoImage.src = "resources/icon.png";

    }
    navigation.classList.toggle('active');
    topBar.classList.toggle('active');
    dashboard_section.classList.toggle('active');
    program_section.classList.toggle('active');
    sessions_section.classList.toggle('active');
    semesters_section.classList.toggle('active');
    courses_section.classList.toggle('active');
    teachers_section.classList.toggle('active');
    room_section.classList.toggle('active');
    allocate_courses_section.classList.toggle('active');
    generate_section.classList.toggle('active');
    print_section.classList.toggle('active');
    admin_section.classList.toggle('active');

};



profilePic.addEventListener('click',()=>{

        toggleMenu.classList.toggle("active");


})

document.addEventListener("click", (event) => {
    // Check if the click is outside both the profilePic and the toggleMenu
    const isClickOutside =
        !profilePic.contains(event.target) && !toggleMenu.contains(event.target);

    if (isClickOutside) {
        toggleMenu.classList.remove("active");
    }
});


// Grab all list items and sections
let navItems = document.querySelectorAll('.navigation li');
let sections = {
    "Dashboard": document.getElementById('dashboard-section'),
    "Programs": document.getElementById('programs-section'),
    "Sessions": document.getElementById('sessions-section'),
    "Semesters": document.getElementById('semesters-section'),
    "Courses": document.getElementById('courses-section'),
    "Teachers": document.getElementById('teachers-section'),
    "Rooms & Labs": document.getElementById('rooms-section'),
    "Allocate Course": document.getElementById('allocate-section'),
    "Generate Timetable": document.getElementById('generate-section'),
    "Print Timetable": document.getElementById('print-section'),
     "Admin Section": document.getElementById('admin-section')

};

document.addEventListener("DOMContentLoaded", () => {

    if (logoutBtn != null){

    logoutBtn.addEventListener('click',logoutUser);
    }
     access = localStorage.getItem("userRole");

    if (loginButton != null){

        guestUserHidden();
        alert("Only Registered Users can add the data guest can only print the timetables. This is read only.")

    }
    else if (access == 1 ){

        superAdminAccess();
        alert("Login Successfully!!...");


    }else if (access == 2) {

        subAdminHidden();
        alert("Login Successfully!!...");

    }

});

// Function to handle section switching
function showSection() {

    refreshCombos();
    // Remove 'hovered' class from all nav items
    navItems.forEach(item => {
        item.classList.remove('hovered');
    });

    // Add 'hovered' class to the clicked item
    this.classList.add('hovered');

    // Hide all sections
    for (let section in sections) {
        sections[section].style.display = 'none';
    }

    // Show the corresponding section
    let sectionToShow = this.id; // Get id of the clicked item
        console.log("Debug: ",this.id);

    sections[sectionToShow].style.display = 'block';

    // Collapse the navigation menu on smaller screens
    if (window.innerWidth <= 768) {
      if (logoImage.src.includes("icon.png")) {
            logoImage.src = "resources/Timetable Xpert logo.png";
        } else {
            logoImage.src = "resources/icon.png";

        }
        navigation.classList.remove('active');
        topBar.classList.remove('active');
        dashboard_section.classList.remove('active');
        program_section.classList.remove('active');
        sessions_section.classList.remove('active');
        semesters_section.classList.remove('active');
        courses_section.classList.remove('active');
        teachers_section.classList.remove('active');
        room_section.classList.remove('active');
        allocate_courses_section.classList.remove('active');
        generate_section.classList.remove('active');
        print_section.classList.remove('active');
        admin_section.classList.remove('active');

    }
}



console.log("Login Button:", loginButton);

// Attach click event to each navigation item
navItems.forEach(item => {
    item.addEventListener('click', showSection);
});

function refreshCombos(){

    //semester section
    semesterFormCombo();
    semesterSessionCombo();
    // course section
    courseProgramSelect();
    fetchCourseSemesterData();
    // teachers section
    departmentCombo();
    // rooms section
    roomDepartmentCombo();
    // allocate section
    allocateProgramCombo();
    fetchSemesterData();
    fetchTeacherData();
    fetchCourseData();
    fetchLabData();



}
function subAdminHidden() {

    printBtn.style.visibility = "hidden";
    downloadBtn.style.visibility = "visible"
    generateButton.style.visibility = "hidden";
    regenerateButton.style.visibility = "hidden";
    resetButton.style.visibility = "hidden";
    admin_btn.style.visibility = "hidden";
    admin_btn.style.visibility = "hidden";
    admin_section.style.visibility = "hidden";

}

function guestUserHidden() {


    subAdminHidden();
    for (let btn of BtnGroup) btn.style.visibility = "hidden";
    for (let btn of delBtns) btn.disabled = true;
    for (let btn of adBtns) btn.disabled = true;
    for (let btn of upBtns) btn.disabled = "true";

}

function superAdminAccess() {

   printBtn.style.visibility = "visible";
    downloadBtn.style.visibility = "hidden"
  generateButton.style.visibility = "visible";
  regenerateButton.style.visibility = "visible";
  resetButton.style.visibility = "visible";

  admin_btn.style.visibility = "visible";
  admin_btn.style.visibility = "visible";
  admin_section.style.visibility = "visible";

    for (let btn of BtnGroup) btn.style.visibility = "visible";
       for (let btn of delBtns) btn.disabled = false;
       for (let btn of adBtns) btn.disabled = false;
       for (let btn of upBtns) btn.disabled = false;
}

function logoutUser() {
    fetch('logout', {
        method: 'GET', // Send a GET request to logout
    })
    .then(response => {
        if (response.ok) {
            // Redirect to login page (or you can use `window.location.href` directly)
            window.location.href = 'index.jsp'; // Assuming index.jsp is the login page
        } else {
            alert('Logout failed. Please try again.');
        }
    })
    .catch(error => {
        console.error('Error during logout:', error);
        alert('An error occurred. Please try again.');
    });
}

// Highlight the first nav item on load
navItems[1].classList.add('hovered');
