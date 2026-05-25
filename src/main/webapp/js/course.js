let courses = [];
let semesters = [];

const courseDataTable = document.getElementById("course_table_view");
const courseIDInput = document.getElementById("course_ID_field");
const courseCodeInput = document.getElementById("course_code_field");
const courseTitleInput = document.getElementById("course_title_field");
const creditHoursInput = document.getElementById("course_cr_combo");
const courseProgramCombo = document.getElementById("course_program_combo");
const semesterSelect = document.getElementById("course_semester_combo");
const updateBtn = document.getElementById("add_course_update_btn");
const deleteBtn = document.getElementById("add_course_delete_btn");
const clearBtn = document.getElementById("add_course_clear_btn");
const addBtn = document.getElementById("add_course_add_btn");
const semTitleHeader = document.getElementById("semester_title_header");
const totalCreditHoursHeader = document.getElementById("total_header");
const allocatedHours = document.getElementById("allocate_header");


function fetchCourseSemesterData(){

// fetch semester data
fetch('/timetablexpert/semester-data')

    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        // Store fetched semester data
        semesters = data.semesterData.map(semester => ({

            id: semester.semesterID,
            title: semester.semesterTitle,
            creditHours: semester.semesterCreditHours,
            section: semester.section,
            capacity: semester.capacity,
            programID: semester.programID,
            semesterID: semester.sID,
            sessionID: semester.sessionID


        }));



    })
    .catch(error => console.error('Error fetching data:', error));
}


    // semester combo based on program

   function searchAndPopulateSemesterDropdown(searchTerm) {

           Array.from(semesterSelect.options).forEach(option => option.remove());



       // Filter semesters based on the programID matching the search term
       const matchingSemesters = semesters.filter(semester => semester.programID == searchTerm);
        console.log("line60: ", matchingSemesters);
         const defaultOption = document.createElement('option');
            defaultOption.value = '-1';
            defaultOption.disabled = true;
            defaultOption.selected = true;
            defaultOption.textContent = '--Select Semester--';
            semesterSelect.appendChild(defaultOption);

       // Create a Set to track unique semester titles
       const seenTitles = new Set();

       // Populate the dropdown with unique semester options
       matchingSemesters.forEach(semester => {
           let semTitle = "";

           // Map semesterID to semester name
           switch (semester.semesterID) {
               case 1:
                   semTitle = "1st Semester";
                   break;
               case 2:
                   semTitle = "2nd Semester";
                   break;
               case 3:
                   semTitle = "3rd Semester";
                   break;
               case 4:
                   semTitle = "4th Semester";
                   break;
               case 5:
                   semTitle = "5th Semester";
                   break;
               case 6:
                   semTitle = "6th Semester";
                   break;
               case 7:
                   semTitle = "7th Semester";
                   break;
               case 8:
                   semTitle = "8th Semester";
                   break;
               default:
                   semTitle = "Incorrect Semester";
                   break;
           }

           // Only add the semester to the dropdown if the title hasn't been added before
           if (!seenTitles.has(semTitle)) {
               seenTitles.add(semTitle);

               // Create and configure option element
               const option = document.createElement('option');
               option.value = semester.semesterID; // Set the value as semesterID
               option.textContent = semTitle; // Display semester name in the dropdown
               semesterSelect.appendChild(option); // Append option to the select element
           }
       });
   }


// Get credit hours for title

function getCreditHoursByTitle(searchValue) {
    const semester = semesters.find(s => s.title.toLowerCase() === searchValue);

    if (semester) {
        return semester.creditHours;
    } else {
        console.log("No semester found with the given title.");
        return null; // or any default value if you want
    }
}

function getAllocateCreditHours(searchValue) {
    let allocatedCr = 0;

    // Iterate over all courses to find the ones that match the search term
    courses.forEach(course => {
        // Check if the course title matches the search value (case-insensitive)
        if ((course.program + " " + course.semester).toLowerCase().includes(searchValue)) {
            allocatedCr += course.creditHours;  // Add the credit hours to the total
        }
    });

    console.log(allocatedCr);
    // Return the total allocated credit hours
    return allocatedCr;   // Return null if no matching course was found
}
// Program Combo

function courseProgramSelect(){


    // Clear existing options and add the default placeholder option
    courseProgramCombo.innerHTML = '<option value="-1" disabled selected>--Select Program--</option>';

    // Fetch program data when the page is loaded
    fetch('/timetablexpert/program-data')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Log the data to verify the structure

            // Populate the select with new options from data.programData
            data.programData.forEach(program => {
                const option = document.createElement('option');
                option.value = program.programID; // Use unique identifier like programID
                option.textContent = program.programName; // Display program name in the combo
                courseProgramCombo.appendChild(option);
            });
        })

        .catch(error => console.error('Error fetching data:', error));
}



displayCourseData();
function displayCourseData() {

    // Fetch the course data from the server
    fetch('/timetablexpert/courseview-data')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {

            // Store fetched course data
            courses = data.courseviewdata.map(course => ({
                id: course.courseID,
                code: course.courseCode,
                title: course.courseTitle,
                creditHours: course.creditHours,
                program: course.program,
                semester: course.semester,
                programID: course.programID,
                semesterID: course.semesterID
            }));


            // Populate the table and initialize event listeners
            populateCourseTable(courses);
            initializeCourseEventListeners();
        })
        .catch(error => console.error('Error fetching data:', error));


}



// Handle insert operation
function handleCourseInsert(event) {
    event.preventDefault();

    // Get input values and trim any leading/trailing spaces
    const courseCode = courseCodeInput.value.trim();
    const courseTitle = courseTitleInput.value.trim();
    const creditHours = creditHoursInput.value;
    const programID = courseProgramCombo.value;
    const semesterID = semesterSelect.value;
    const program = courseProgramCombo[courseProgramCombo.selectedIndex].text;
    const semester = semesterSelect[semesterSelect.selectedIndex].text;

    // Validate input fields
    if (!courseCode || !courseTitle || !creditHours || !program || !semester) {
        alert("All fields must be filled out.");
        return;
    }



    // Check if a course with the same title, program, and semester already exists in the `courses` array
    const existingCourse = courses.find(course =>
        course.title === courseTitle &&
        course.program === program &&
        course.semester === semester
    );

    if (existingCourse) {
        alert(`Error: Course with title "${courseTitle}" already exists in the program "${program}" and semester "${semester}".`);
        return;
    }

    let lab = 0;
    let nonLab = 0;

    if (creditHours == 0) {

        lab = 0;
        nonLab = 1;
    } else if (creditHours == 1) {

        lab = 0;
        nonLab = 2;
    } else if (creditHours == 2) {

        lab = 0;
        nonLab = 3;
    } else if (creditHours == 3) {

        lab = 1;
        nonLab = 2;

    } else if (creditHours == 4) {
        lab = 1;
        nonLab = 3;
    } else if (creditHours == 5) {

        lab = 0;
        nonLab = 4;

    } else if (creditHours == 6) {

        lab = 2;
        nonLab = 0;
    }

    let allocate = getAllocateCreditHours((program + " " + semester).toLowerCase());
    console.log(allocate, " :", allocate + (lab + nonLab) > totalCreditHoursHeader);
    if ((allocate + (lab + nonLab)) > totalCreditHoursHeader) {

        alert(`Error: ${program} ${semester} total Credit Hours is already allocated.`);
        return;


    }
    // Create a new course object to send to the server
    const newCourse = {

        courseCode,
        courseTitle,
        creditHours,
        program,
        semester,
        programID,
        semesterID
    };

    // Send POST request to insert the new course
    fetch('/timetablexpert/courseview-data', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newCourse)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to insert course');
            }
            return response.json();
        })
        .then(data => {
            alert("Course added successfully!");

            clearCourse();
            displayCourseData();

        })
        .catch(error => console.error('Error inserting course:', error));
}


function hanfleCourseUpdate(event) {
    event.preventDefault();

 let lab = 0;
    let nonLab = 0;
    // Get input values and trim any leading/trailing spaces
    const courseID = courseIDInput.value.trim();
    const courseCode = courseCodeInput.value.trim();
       const courseTitle = courseTitleInput.value.trim();
       const creditHours = creditHoursInput.value;
       const programID = courseProgramCombo.value;
       const semesterID = semesterSelect.value;
       const program = courseProgramCombo[courseProgramCombo.selectedIndex].text;
       const semester = semesterSelect[semesterSelect.selectedIndex].text;

    // Validate input fields
    if (!courseCode || !courseTitle || !creditHours || !programID || !semester) {

        alert("All fields must be filled out.");
        return;
    }

  let allocate = getAllocateCreditHours((program + " " + semester).toLowerCase());
    console.log(allocate, " :", allocate + (lab + nonLab) > totalCreditHoursHeader);
    if ((allocate + (lab + nonLab)) > totalCreditHoursHeader) {

        alert(`Error: ${program} ${semester} total Credit Hours is already allocated.`);
        return;


    }


    // Create an updated course object to send to the server
    const updatedCourse = {
         courseCode,
         courseTitle,
                creditHours,
                program,
                semester,
                programID,
                semesterID
    };


    // Send PUT request to update the course
    fetch(`/timetablexpert/courseview-data/${courseID}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedCourse)
    })
        .then(response => {
            if (!response.ok) {
                 throw new Error(errorData.message || 'Failed to update course');

            }
            return response.json();
        })
        .then(data => {

            alert("Course updated successfully!");
            displayCourseData();
            clearCourse();

        })
        .catch(error => console.error('Error updating course:', error));
}


// Function to populate the table
function populateCourseTable(data) {
    const tableBody = document.querySelector("#course_table_view tbody");
    tableBody.innerHTML = ""; // Clear existing rows

    data.forEach(course => {
        const row = courseCreateRow(course);
        tableBody.appendChild(row);
    });
}

// Function to create a table row
function courseCreateRow(course) {
    const row = document.createElement("tr");

    const idCell = document.createElement("td");
    idCell.textContent = course.id;
    row.appendChild(idCell);

    const codeCell = document.createElement("td");
    codeCell.textContent = course.code;
    row.appendChild(codeCell);

    const titleCell = document.createElement("td");
    titleCell.textContent = course.title;
    row.appendChild(titleCell);

    const creditHoursCell = document.createElement("td");
    creditHoursCell.textContent = course.creditHours;
    row.appendChild(creditHoursCell);

    const programCell = document.createElement("td");
    programCell.textContent = course.program;
    row.appendChild(programCell);

    const semesterCell = document.createElement("td");
    semesterCell.textContent = course.semester;
    row.appendChild(semesterCell);

    return row;
}

// Function to handle sorting based on table header clicks
function handleHeaderClick() {

    const column = this.dataset.column;
    const order = this.dataset.order;
    let newOrder = order === 'asc' ? 'desc' : 'asc';
    this.dataset.order = newOrder;

    courses.sort((a, b) => {
        if (newOrder === 'asc') {
            return a[column] > b[column] ? 1 : -1;
        } else {
            return a[column] < b[column] ? 1 : -1;
        }
    });

    populateCourseTable(courses);
}

// Function to search through courses
function searchCourseTable(value, data) {
    return data.filter(course => {
        const searchValue = value.toLowerCase();

        // Check if the search value is included in any course property
        return (
            course.title.toLowerCase().includes(searchValue) ||
            course.code.toLowerCase().includes(searchValue) ||
            course.program.toLowerCase().includes(searchValue) ||
            course.semester.toLowerCase().includes(searchValue) ||
            course.creditHours.toString().toLowerCase().includes(searchValue)
        );
    });
}
// Function to handle search input
function handleCourseSearch() {
    const value = this.value;
    const filteredCourses = searchCourseTable(value, courses);
    populateCourseTable(filteredCourses);
}



// Initialize event listeners for sorting, search, and table actions
function initializeCourseEventListeners() {

    courseProgramSelect();
    const headers = document.querySelectorAll('th');
    headers.forEach(header => {
        header.addEventListener('click', handleHeaderClick);
    });

    const searchInput = document.getElementById('search-input');
    searchInput.addEventListener('keyup', handleCourseSearch);

    addBtn.addEventListener("click", handleCourseInsert);


    updateBtn.addEventListener("click", hanfleCourseUpdate);
    deleteBtn.addEventListener("click", handleCourseDelete);

    updateBtn.style.visibility = "hidden";
    deleteBtn.style.visibility = "hidden";

    courseProgramCombo.addEventListener("change", handleCourseComboFilter);
    semesterSelect.addEventListener("change", handleCourseComboFilter);

}

// Clear input fields
function clearCourse() {

    courseIDInput.value = "";
    courseCodeInput.value = "";
    courseTitleInput.value = "";
    creditHoursInput.value = "";
    courseProgramCombo.value = courseProgramCombo.options[0].value;
    semesterSelect.value = semesterSelect.options[0].value;



    semTitleHeader.innerText = "";
    totalCreditHoursHeader.innerText = "0";
    allocatedHours.innerText = "0";
    populateCourseTable(courses);



}

clearBtn.addEventListener("click", clearCourse);


// Handle update operation

// Update the course in the local array and refresh the table
function updateCourseInTable(updatedCourse) {
    const index = courses.findIndex(course => course.id === updatedCourse.courseID);
    if (index !== -1) {
        courses[index].title = updatedCourse.courseTitle;
        courses[index].creditHours = updatedCourse.creditHours;
        courses[index].program = updatedCourse.program;
        courses[index].semester = updatedCourse.semester;
        courses[index].programID = updatedCourse.programID;
        courses[index].semesterID = updatedCourse.semesterID;
        populateCourseTable(courses);
    }
}

// Handle delete operation
function handleCourseDelete(event) {
    event.preventDefault();
    const courseID = courseIDInput.value.trim();

    if (!courseID) {
        alert("Course ID cannot be empty.");
        return;
    }

    fetch(`/timetablexpert/courseview-data/${courseID}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete course');
            }
            return response.json();
        })
        .then(data => {
            alert("Course deleted successfully!");
            displayCourseData();
            clearCourse();
        })
        .catch(error => console.error('Error deleting course:', error));
}

// Remove the deleted course from the local array and refresh the table
function removeCourseFromTable(courseID) {
    courses = courses.filter(course => course.id !== Number(courseID));
    populateCourseTable(courses);
}

function handleCourseComboFilter() {

    let filteredCourses = null;

    // Get selected values from the dropdowns
    const selectedCourseProgram = courseProgramCombo.options[courseProgramCombo.selectedIndex].text;
    const selectedIndex = courseProgramCombo.value;
    const selectedSemIndex = semesterSelect.value
    const selectedSemester = semesterSelect.options[semesterSelect.selectedIndex].text;
    console.log("selected Semester value: ", semesterSelect.value);

    // Apply filters based on selected combinations
    if (selectedIndex !== "-1" && selectedSemIndex !== "-1") {

        searchAndPopulateSemesterDropdown(courseProgramCombo.value);
         semesterSelect.value = selectedSemIndex;
        console.log("selected Semester:1 ", semesterSelect.value);

        totalCreditHoursHeader.innerText = getCreditHoursByTitle((selectedCourseProgram + " " + selectedSemester).toLowerCase());
        allocatedHours.innerText = getAllocateCreditHours((selectedCourseProgram + " " + selectedSemester).toLowerCase());
        // Both Program and Semester are selected
        semTitleHeader.innerText = selectedCourseProgram + " " + selectedSemester;
        filteredCourses = courses.filter(course =>
            course.program === selectedCourseProgram && course.semester === selectedSemester
        );
    } else if (selectedIndex != "-1") {
        // Only Program is selected

        console.log(courseProgramCombo.value);
        searchAndPopulateSemesterDropdown(courseProgramCombo.value);

        semTitleHeader.innerText = selectedCourseProgram;

        filteredCourses = courses.filter(course =>
            course.program === selectedCourseProgram

        );
        console.log(filteredCourses);



    }
    else if (selectedSemIndex !== "-1") {

        filteredCourses = courses.filter(course => {
            const courseSemester = course.semester.trim().toLowerCase();
            const selectedSemesterNormalized = selectedSemester.trim().toLowerCase();

            console.log(courseSemester, "=== ", selectedSemesterNormalized, courseSemester === selectedSemesterNormalized);
            return courseSemester === selectedSemesterNormalized;
        }
        );
    }

    console.log(filteredCourses);
    populateCourseTable(filteredCourses);


    // Populate table with filtered courses




}


let courseSelectedRow = null; // To track selected row

//
courseDataTable.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {


        updateBtn.style.visibility = "visible";
        deleteBtn.style.visibility = "visible";
        const row = target.parentNode;

        if (courseSelectedRow) {
            courseSelectedRow.classList.remove("selected-row");
        }

        courseSelectedRow = row;
        courseSelectedRow.classList.add("selected-row");

        const cells = courseSelectedRow.getElementsByTagName("td");


        courseIDInput.value = cells[0].textContent;
        courseCodeInput.value = cells[1].textContent;
        courseTitleInput.value = cells[2].textContent;
        semTitleHeader.innerText = cells[4].textContent + " " + cells[5].textContent;

        totalCreditHoursHeader.innerText = getCreditHoursByTitle((cells[4].textContent + " " + cells[5].textContent).toLowerCase());
        allocatedHours.innerText = getAllocateCreditHours((cells[4].textContent + " " + cells[5].textContent).toLowerCase());


    }

    event.stopPropagation();
});
// Handle click outside table to clear selection
document.addEventListener("click", (event) => {

    const isClickInsideTable = courseDataTable.contains(event.target);
    const isClickInsideInputs = document.querySelector('.form-wrapper-course').contains(event.target); // Adjust selector as needed



    // Only proceed to hide buttons if the click is outside the table, inputs, and combo box
    if (!isClickInsideTable && !isClickInsideInputs && courseSelectedRow) {
        if (courseSelectedRow) {
            courseSelectedRow.classList.remove("selected-row");
        }
        courseSelectedRow = null;

        updateBtn.style.visibility = "hidden";
        deleteBtn.style.visibility = "hidden";
        clearCourse();
    }
});