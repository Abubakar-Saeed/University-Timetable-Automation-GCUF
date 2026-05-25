let allocateData = [];
let allocateSemesters = [];
let filterSemesters = [];
let allocateCourses = [];
let allocateFilterCourses = [];
let allocateTeachers = [];
let filterAllocateTeachers = [];
let allocateLabData = [];
let filteredLabData = [];
let lectureView = [];
let allocated_Hours = [];
let teacherCourses = [];
let isLabCourse;


const allocateTable = document.getElementById("allocate_table_view");
// Select input fields and dropdowns
const allocateID = document.getElementById("allocate_ID_field");
const allocateProgramSelect = document.getElementById("allocate_program_combo");
const batchSelect = document.getElementById("allocate_batch_combo");
const semesterSelectAllocate = document.getElementById("allocate_semester_combo");
const courseSelect = document.getElementById("course_combo");
const allocateTypeSelect = document.getElementById("allocate_type_combo");
const teacherSelect = document.getElementById("teacher_combo");
const labSelect = document.getElementById("lab_combo");
const allocateIDField = document.getElementById("allocate_ID_field");

// Select buttons
const addButton = document.getElementById("add_allocate_add_btn");
const deleteButton = document.getElementById("add_allocate_delete_btn");
const clearButton = document.getElementById("add_allocate_clear_btn");


// Select header information
const allocateTitleHeader = document.getElementById("allocate_title_header");
const allocatedHoursHeader = document.getElementById("allocate_cr_header");
const total_Credit_Hours_Header = document.getElementById("total_cr_header");


labSelect.disabled = true;
displayAllocateData();

function displayAllocateData() {
    fetch('/timetablexpert/allocate-data')

        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Store fetched semester data
            allocateData = data.allocateData.map(allocate => ({

                subjectID: allocate.subjectID,
                subjectTitle: allocate.subjectTitle,
                batch: allocate.batch,
                lab: allocate.lab,
                programName: allocate.programName,
                semester: allocate.semester

            }));

            initializeAllocateEventListeners();
            populateAllocateTable(allocateData);
        })
        .catch(error => console.error('Error fetching data:', error));
}

function addAllocate() {


    const subjectTitle = courseSelect.value.trim();
    const batch = batchSelect.value.trim();
    const lab = labSelect.value.trim();
    const programName = allocateProgramSelect.value.trim();
    const semester = semesterSelectAllocate.value.trim();
    const typeTeacher = allocateTypeSelect.value.trim();
    const teacherCombo = teacherSelect.value.trim();

        let subject = allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent + " "
        + semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent + " "
        + courseSelect[courseSelect.selectedIndex].textContent + " ("
        + teacherSelect[teacherSelect.selectedIndex].textContent + ")";

 // Validate fields
    if ( subjectTitle == '-1' || batch == '-1'  || programName == '-1'|| semester == '-1'|| typeTeacher == '-1'|| teacherCombo == '-1') {

        alert("All fields must be filled out.");
        return;

    }
       // Validate lab field only if it is enabled
    if (!labSelect.disabled && lab == '-1') {

            alert("Please select a valid lab.");
            labSelect.focus();
            return;

        }




    const matchingProgramSemester = allocateData.find(allocate =>
                allocate.subjectTitle == subject && allocate.batch == batchSelect[batchSelect.selectedIndex].textContent
        )


      console.log("Matching : ", matchingProgramSemester);
    // checking if course is already allocated
        if(matchingProgramSemester){

            alert(`Subject: ${subject} is already exist`);
            return;
        }

  const matchingTeachingCourse = teacherCourses.find(teacher =>

            teacher.lectureID == teacherCombo && teacher.semesterID == semester


    )


    console.log("Matching Teacher: ", matchingTeachingCourse);
// checking if course is already allocated
    if(matchingTeachingCourse){

        alert(`Subject: ${allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent} ${semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent} ${courseSelect[courseSelect.selectedIndex].textContent} is already allocated`)
        return;

    }


     const matchingTeacher = lectureView.find(lecture =>
                 lecture.lectureID == teacherCombo

      );

     let teacherCreditHours = matchingTeacher.creditHours;

     const matchingCourse = allocateCourses.find(course =>

            course.id == courseSelect.value
     )

     let courseCreditHours = matchingCourse.creditHours;
// check if teacher have enough credit hours
    if ((teacherCreditHours + courseCreditHours) > 12){

        alert(`${teacherSelect[teacherSelect.selectedIndex].textContent} is already allocated 12 credit hours . A teacher cannot be allocated more than 12 credit hours.`);
        return;

    }



    const newAllocation = {

        subjectTitle: subject,
        batch: batchSelect[batchSelect.selectedIndex].textContent,
        lab: labSelect.value !== '-1' ? labSelect[labSelect.selectedIndex].textContent : "",
        programName: allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent,
        semester: allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent + " " + semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent

    };

    console.log("New Allocation: ", newAllocation);


  fetch('/timetablexpert/allocate-data', {
      method: 'POST',
      headers: {
          'Content-Type': 'application/json'
      },
      body: JSON.stringify(newAllocation)
  })
      .then(response => {
          // Check if the response is successful (status code 200-299)
          if (!response.ok) {
              return response.json().then(errorData => {
                  // Use server-provided error message if available
                  throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
              });
          }
          return response.json();
      })
      .then(data => {
          // Success case
          alert(data.message || 'Allocation added successfully!');
          displayAllocateData();
      })
      .catch(error => {
          // Catch both network errors and server errors
          console.error('Error:', error.message);
          alert(`An error occurred: ${error.message}`);
      });
      clearFields();
}

function handleAllocateDelete() {

    let ID = allocateID.value.trim();

    fetch(`/timetablexpert/allocate-data/${ID}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    // Handle server-provided error messages
                    throw new Error(errorData.message || 'Failed to delete allocation');
                });
            }
            return response.json();
        })
        .then(data => {
            // Display a success message
            alert(data.message || 'Allocation deleted successfully!');

            // Refresh the allocate data table
            displayAllocateData();
        })
        .catch(error => {
            console.error('Error deleting allocation:', error.message);
            alert(`An error occurred: ${error.message}`);
        });

        clearFields();
}




function populateAllocateTable(data) {
    const tableBody = document.querySelector("#allocate_table_view tbody");
    tableBody.innerHTML = ""; // Clear existing rows

    data.forEach(allocation => {
        const row = createAllocateRow(allocation);
        tableBody.appendChild(row);
    });
}

// Function to create a row for the Allocate Course table
function createAllocateRow(allocation) {
    const row = document.createElement("tr");

    // Subject ID cell
    const subjectIdCell = document.createElement("td");
    subjectIdCell.textContent = allocation.subjectID;
    row.appendChild(subjectIdCell);

    // Subject Title cell
    const subjectTitleCell = document.createElement("td");
    subjectTitleCell.textContent = allocation.subjectTitle;
    row.appendChild(subjectTitleCell);



    // Batch cell
    const batchCell = document.createElement("td");
    batchCell.textContent = allocation.batch;
    row.appendChild(batchCell);
    // Lab cell
    const labCell = document.createElement("td");
    labCell.textContent = allocation.lab ? allocation.lab : "No"; // Convert boolean to text
    row.appendChild(labCell);

    return row;
}

// Function to search through allocation data
function searchAllocateTable(value, data) {
    return data.filter(allocation =>
        allocation.subjectTitle.toLowerCase().includes(value.toLowerCase()) ||
        allocation.batch.toLowerCase().includes(value.toLowerCase()) ||
        (allocation.lab ? allocation.lab : "no").toLowerCase().includes(value.toLowerCase())



    );
}

// Function to handle search input
function handleAllocateSearch() {
    const value = this.value; // Get the search input value
    const filteredAllocations = searchAllocateTable(value, allocateData); // Use the allocation data
    populateAllocateTable(filteredAllocations); // Populate the table with the filtered results
}



function initializeAllocateEventListeners() {

    const searchInputAllocate = document.getElementById('search-input');
    searchInputAllocate.addEventListener('keyup', handleAllocateSearch);
    allocateProgramSelect.addEventListener('change', handleAllocateComboFilter);
    batchSelect.addEventListener('change', handleAllocateComboFilter);
    semesterSelectAllocate.addEventListener('change', handleAllocateComboFilter);
    allocateTypeSelect.addEventListener('change', handleAllocateComboFilter);
    courseSelect.addEventListener('change', updateLabSelectStatus);
    addButton.addEventListener('click', addAllocate);
    clearButton.addEventListener('click', clearFields );
    deleteButton.addEventListener('click',handleAllocateDelete);
    allocateProgramCombo();

}

function handleAllocateComboFilter() {

    let filteredAllocations = allocateData;
    let semIndex = semesterSelectAllocate.value;
    let courseIndex = courseSelect.value;
    let teacherIndex = teacherSelect.value;

    if (batchSelect.value != '-1' && allocateProgramSelect.value != '-1') {


        filterSemesters = allocateSemesters.filter(semester => {
            return semester.programID == allocateProgramSelect.value && semester.section == batchSelect[batchSelect.selectedIndex].textContent

        })

        semesterCombo();
        semesterSelectAllocate.value = semIndex;

    }

    if (allocateTypeSelect.value != '-1' && allocateProgramSelect.value != '-1') {


        filterAllocateTeachers = allocateTeachers.filter(teacher =>
            teacher.type == allocateTypeSelect[allocateTypeSelect.selectedIndex].textContent
        );

        teacherCombo();
        teacherSelect.value = teacherIndex;

    }
    if (semesterSelectAllocate.value != '-1') {

        allocateFilterCourses = allocateCourses.filter(course => {

            return course.program == allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent && course.semester == semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent;

        })



        courseCombo();
        courseSelect.value = courseIndex;
        allocate_title_header.innerText = allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent + " " + semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent + " (" + batchSelect[batchSelect.selectedIndex].textContent + ")";

        const selectedProgram = allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent;
        const selectedSemester = semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent;
        const selectedBatch = batchSelect[batchSelect.selectedIndex].textContent;

        const matchingSemester = allocateSemesters.find(semester =>
            semester.title === `${selectedProgram} ${selectedSemester}` &&
            semester.section === selectedBatch
        );

      // Find the matching allocation
      const matchingAllocatedHours = allocated_Hours.find(allocate =>
                 allocate.semesterID == semesterSelectAllocate.value

      );

      // Update the header text with matching hours or default to "0"
      allocatedHoursHeader.innerText = matchingAllocatedHours
          ? matchingAllocatedHours.allocateHours
          : "0";

        total_Credit_Hours_Header.innerText = matchingSemester
            ? matchingSemester.creditHours
            : "0";
    }



    // Program + Batch + Semester
     if (allocateProgramSelect.value != '-1' && batchSelect.value != '-1' && semesterSelectAllocate.value != '-1') {

        filteredAllocations = filteredAllocations.filter(allocation =>
            allocation.programName == allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent &&
            allocation.batch == batchSelect[batchSelect.selectedIndex].textContent &&
            allocation.semester == allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent + " " + semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent
        );



        populateAllocateTable(filteredAllocations);

    }


    // Program + Semester
    else if (allocateProgramSelect.value != '-1' && semesterSelectAllocate.value != '-1') {
        filteredAllocations = filteredAllocations.filter(allocation =>
            allocation.programName == allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent &&
            allocation.semesterID == semesterSelectAllocate.value
        );

    }



    // Batch + Semester
    else if (batchSelect.value != '-1' && semesterSelectAllocate.value != '-1') {
        filteredAllocations = filteredAllocations.filter(allocation =>
            allocation.batch == batchSelect[batchSelect.selectedIndex].textContent &&
            allocation.semesterID == semesterSelectAllocate.value
        );
    }
    else if (allocateProgramSelect.value != '-1' && batchSelect.value !== '-1') {
        filteredAllocations = filteredAllocations.filter(allocation =>
            allocation.programName == allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent &&
            allocation.batch == batchSelect[batchSelect.selectedIndex].textContent
        );



    }
    // Filter by Program
    else if (allocateProgramSelect.value != '-1') {
        filteredAllocations = filteredAllocations.filter(allocation =>
            allocation.programName == allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent
        );


        allocate_title_header.innerText = allocateProgramSelect[allocateProgramSelect.selectedIndex].textContent;




    }

    // Filter by Batch
    else if (batchSelect.value != '-1') {
        filteredAllocations = allocateData.filter(allocation =>

            allocation.batch == batchSelect[batchSelect.selectedIndex].textContent


        );

        populateAllocateTable(filteredAllocations);
    }


    // Filter by Semester
    else if (semesterSelectAllocate.value != '-1') {

        filteredAllocations = filteredAllocations.filter(allocation =>
            allocation.semesterID == semesterSelectAllocate.value
        );

    }




        populateAllocateTable(filteredAllocations);

}


function fetchSemesterData(){

    fetch('/timetablexpert/semester-data')

            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Store fetched semester data
                allocateSemesters = data.semesterData.map(semester => ({

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
function fetchCourseData(){

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
        allocateCourses = data.courseviewdata.map(course => ({

            id: course.courseID,
            code: course.courseCode,
            title: course.courseTitle,
            creditHours: course.creditHours,
            program: course.program,
            semester: course.semester,
            programID: course.programID,
            semesterID: course.semesterID

        }));



    })
    .catch(error => console.error('Error fetching data:', error));
}

function allocateProgramCombo(){

    // Clear existing options and add the default placeholder option
    allocateProgramSelect.innerHTML = '<option value="-1" disabled selected>--Select Program--</option>';

    // Fetch program d                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  ata when the page is loaded
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
                allocateProgramSelect.appendChild(option);

            });
        })
        .catch(error => console.error('Error fetching data:', error));
}



// Semester Combo
function semesterCombo() {
    // Ensure the semesterSelectAllocate element is defined
    console.log("Selected Before: ", semesterSelectAllocate[semesterSelectAllocate.selectedIndex].textContent)

    if (!semesterSelectAllocate) {
        console.error('semesterSelectAllocate element not found.');
        return;
    }

    // Clear all current options except the placeholder
    while (semesterSelectAllocate.options.length > 0) {
        semesterSelectAllocate.remove(0);
    }

    // Add the default placeholder option
    const defaultOption = document.createElement('option');
    defaultOption.value = '-1';
    defaultOption.disabled = true;
    defaultOption.selected = false;
    defaultOption.textContent = '--Select Semester--';
    semesterSelectAllocate.appendChild(defaultOption);

    // Add options for each semester in the filtered list
    filterSemesters.forEach(semester => {
        let sem;
        switch (semester.semesterID) {
            case 1: sem = "1st Semester"; break;
            case 2: sem = "2nd Semester"; break;
            case 3: sem = "3rd Semester"; break;
            case 4: sem = "4th Semester"; break;
            case 5: sem = "5th Semester"; break;
            case 6: sem = "6th Semester"; break;
            case 7: sem = "7th Semester"; break;
            case 8: sem = "8th Semester"; break;
            default: sem = "Unknown Semester";
        }

        const option = document.createElement('option');
        option.value = semester.id; // Set value to semester ID
        option.textContent = sem;   // Set text content to semester name
        semesterSelectAllocate.appendChild(option); // Append option to dropdown
    });

    // Ensure dropdown is visible and properly selected
    semesterSelectAllocate.removeAttribute('disabled');


}
function courseCombo() {
    // Ensure the semesterSelectAllocate element is defined
    console.log("Hello everyone");
    if (!courseSelect) {
        console.error('course Combo element not found.');
        return;
    }

    // Clear all current options except the placeholder
    while (courseSelect.options.length > 0) {
        courseSelect.remove(0);
    }

    // Add the default placeholder option
    const defaultOption = document.createElement('option');
    defaultOption.value = '-1';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    defaultOption.textContent = '--Select Course--';
    courseSelect.appendChild(defaultOption);

    // Add options for each semester in the filtered list
    allocateFilterCourses.forEach(course => {


        const option = document.createElement('option');
        option.value = course.id; // Set value to semester ID
        option.textContent = course.title;   // Set text content to semester name
        courseSelect.appendChild(option); // Append option to dropdown
    });

    // Ensure dropdown is visible and properly selected
    courseSelect.removeAttribute('disabled');


}
function teacherCombo() {
    // Ensure the semesterSelectAllocate element is defined
    if (!teacherSelect) {
        console.error('teacher Combo element not found.');
        return;
    }

    // Clear all current options except the placeholder
    while (teacherSelect.options.length > 0) {
        teacherSelect.remove(0);
    }

    // Add the default placeholder option
    const defaultOption = document.createElement('option');
    defaultOption.value = '-1';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    defaultOption.textContent = '--Select Teacher--';
    teacherSelect.appendChild(defaultOption);

    // Add options for each semester in the filtered list
    filterAllocateTeachers.forEach(teacher => {


        const option = document.createElement('option');
        option.value = teacher.teacherID; // Set value to semester ID
        option.textContent = teacher.teacherName;   // Set text content to semester name
        teacherSelect.appendChild(option); // Append option to dropdown
    });

    // Ensure dropdown is visible and properly selected
    teacherSelect.removeAttribute('disabled');


}
function labCombo() {
    // Ensure the semesterSelectAllocate element is defined
    if (!labSelect) {
        console.error('lab Combo element not found.');
        return;
    }

    // Clear all current options except the placeholder
    while (labSelect.options.length > 0) {
        labSelect.remove(0);
    }

    // Add the default placeholder option
    const defaultOption = document.createElement('option');
    defaultOption.value = '-1';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    defaultOption.textContent = '--Select Lab--';
    labSelect.appendChild(defaultOption);

    // Add options for each semester in the filtered list
    filteredLabData.forEach(lab => {


        const option = document.createElement('option');
        option.value = lab.roomID; // Set value to semester ID
        option.textContent = lab.roomNo;   // Set text content to semester name
        labSelect.appendChild(option); // Append option to dropdown
    });

    // Ensure dropdown is visible and properly selected
    labSelect.removeAttribute('disabled');


}

function clearFields(){



  // Clear text inputs
    allocateID.value = "";

    // Reset dropdowns to their default options
    allocateProgramSelect.value = allocateProgramSelect.options[0].value;

    batchSelect.value = batchSelect.options[0].value;
    semesterSelectAllocate.value = semesterSelectAllocate.options[0].value;
    courseSelect.value = courseSelect.options[0].value;
    allocateTypeSelect.value = allocateTypeSelect.options[0].value;
    teacherSelect.value = teacherSelect.options[0].value;
    labSelect.value = labSelect.options[0].value;

    // Clear headers or reset to default values
    allocateTitleHeader.innerText = "";
    allocatedHoursHeader.innerText = "0";
    total_Credit_Hours_Header.innerText = "0";

    populateAllocateTable(allocateData);
}




// Teacher Data
function fetchTeacherData(){
// Fetch the teacher data from the server
fetch('/timetablexpert/teacher-data')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {

        // Store fetched teacher data
        allocateTeachers = data.teacherData.map(teacher => ({

            teacherID: teacher.teacherID,
            teacherName: teacher.teacherName,
            phoneNo: teacher.phoneNo,
            email: teacher.email,
            gender: teacher.gender,
            department: teacher.department,
            type: teacher.type,

        }));



    })
    .catch(error => console.error('Error fetching data:', error));
}


function fetchLabData(){

    // Lab Data
        fetch('/timetablexpert/room-data')
            .then(response => response.json())
            .then(data => {


                console.log(data);
                allocateLabData = data.labData.map(room => ({

                    roomID: room.roomID,
                    roomNo: room.roomNo,
                    capacity: room.capacity,
                    program: room.program,
                    programID: room.programID,
                    type: room.type

                }));



            })
            .catch(error => console.error("Error fetching data:", error));
}





// Validation Data

fetch('/timetablexpert/allocate-data?action=validation')

    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {


                lectureView = data.lectureViewData.map(lecture => ({

                    lectureID: lecture.lectureID,
                    creditHours: lecture.creditHours,

                }));
                   teacherCourses = data.teacherCourses.map(teacher => ({

                                    teacherID: teacher.teacherCourseID,
                                    title: teacher.title,
                                    lectureID: teacher.lectureID,
                                    courseID: teacher.courseID,
                                    semesterID: teacher.programSemesterID

                                }));

                       allocated_Hours = data.allocateHoursData.map(allocate => ({

                                                       semesterID: allocate.semesterID,
                                                       allocateHours: allocate.allocated_Hours

                                                   }));



    })
    .catch(error => console.error('Error fetching data:', error));
let allocateSelectedRow = null;

allocateTable.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {


        deleteButton.style.visibility = "visible";
        const row = target.parentNode;

        if (allocateSelectedRow) {
            allocateSelectedRow.classList.remove("selected-row");
        }

        allocateSelectedRow = row;
        allocateSelectedRow.classList.add("selected-row");

        const cells = allocateSelectedRow.getElementsByTagName("td");


        // Populate the form inputs with the selected row's data
        allocateID.value = cells[0].textContent;

    }

    event.stopPropagation();
});

document.addEventListener("click", (event) => {

    const isClickInsideTable = allocateTable.contains(event.target);
    const isClickInsideInputs = document.querySelector('.form-wrapper-allocate').contains(event.target); // Adjust selector as needed



    // Only proceed to hide buttons if the click is outside the table, inputs, and combo box
    if (!isClickInsideTable && !isClickInsideInputs && allocateSelectedRow) {
        if (allocateSelectedRow) {
            allocateSelectedRow.classList.remove("selected-row");
        }
        allocateSelectedRow = null;



        deleteButton.style.visibility = "hidden";
        clearFields();

    }
});

async function updateLabSelectStatus() {
    const courseTitle = courseSelect[courseSelect.selectedIndex].textContent;
    const courseExists = await searchCourse(courseTitle); // Await the result
    console.log ("in status: ", courseExists);
          filteredLabData = allocateLabData.filter (lab=>
                                  lab.programID == allocateProgramSelect.value
                              );

    labCombo();
    labSelect.disabled = !courseExists;
}

const searchCourse = async (coursetitle) => {
    try {
        const response = await fetch(`/timetablexpert/allocate-data?action=search&coursetitle=${encodeURIComponent(coursetitle)}`);
        if (response.ok) {
            const data = await response.json();
            console.log('Course exists: ', data.exists);
            return Boolean(data.exists);;
        } else {
            console.error('Error:', await response.text());
        }
    } catch (error) {
        console.error('Fetch error:', error);
    }
};


