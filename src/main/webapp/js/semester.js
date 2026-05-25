let semestersData = [];

const semestersDataTable = document.getElementById("semester_table_view");
const semesterIDInput = document.getElementById("semester_id_field");
const semesterTitleInput = document.getElementById("semester_combo");
const semesterCreditHoursInput = document.getElementById("semester_credit_hours");
const sectionInput = document.getElementById("semester_batch_combo");
const capacityInput = document.getElementById("semester_capacity");
const semesterUpdateBtn = document.getElementById("add_semester_update_btn");
const semesterDeleteBtn = document.getElementById("add_semester_delete_btn");
const semesterClearBtn = document.getElementById("add_semester_clear_btn");
const semesterProgramSelect = document.getElementById('semester_program_combo');
const sessionSelect = document.getElementById('semester_session_combo');


// Fetch the semester data from the server
fetch('/timetablexpert/semester-data')

    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        // Store fetched semester data
        semestersData = data.semesterData.map(semester => ({

            id: semester.semesterID,
            title: semester.semesterTitle,
            creditHours: semester.semesterCreditHours,
            section: semester.section,
            capacity: semester.capacity,
            programID: semester.programID,
            semesterID: semester.sID,
            sessionID: semester.sessionID


        }));


        // Populate the table and initialize event listeners
        populateSemesterTable(semestersData);
        initializeSemesterEventListeners();
    })
    .catch(error => console.error('Error fetching data:', error));

 // Handle insert operation
function handleInsert(event) {
        event.preventDefault(); // Prevent form submission

        // Get input values and trim any leading/trailing spaces
        const creditHours = semesterCreditHoursInput.value.trim();
        const capacity = capacityInput.value.trim();
        const section = sectionInput.value.trim();
        const semesterTitle = semesterTitleInput.value;
        const programID = semesterProgramSelect.value.trim();
        const sessionID = sessionSelect.value.trim();

         const programTitle = semesterProgramSelect.options[semesterProgramSelect.selectedIndex].text.trim();
         const sTitle = semesterTitleInput.options[semesterTitleInput.selectedIndex].text.trim();
         const batch = sectionInput.options[sectionInput.selectedIndex].text.trim();

            const title = `${programTitle} ${sTitle}`;

            // Search for matching semester in the array
            const foundSemester = semestersData.find(semester =>
                semester.title == title && semester.section == batch
            );



        // Validate input fields
        if ( !semesterTitle || !creditHours || !section || !capacity || !programID || !sessionID) {
            alert("All fields must be filled out.");
            return;
        } else if (section <= 0 || capacity <= 0) {
            alert("Capacity and semester section must be positive.");
            return;
        } else if(foundSemester){

            alert("Semester Name: " + title + " " + batch + " is already exist");
            return;
        }

        // Create a new semester object to send to the server
        const newSemester = {
               semesterTitle: title,
                  semesterCreditHours: creditHours,
                       section: section,
                       capacity: capacity,
                       programID: programID,
                       sID: semesterTitle,
                       sessionID: sessionID
        };

        // Send POST request to insert the new semester
        fetch('/timetablexpert/semester-data', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newSemester)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to insert semester');
            }
            return response.json();
        })
        .then(data => {
            alert("Semester added successfully!");

            // Add the new semester to the local semestersData array
            semestersData.push({

                  id: data.semesterID,
                  title: data.semesterTitle,
                  creditHours: data.semesterCreditHours,
                  section: data.section == 1 ? "Morning" : "Replica",
                  capacity: data.capacity,
                  programID: data.programID,
                  semesterID: data.sID,
                  sessionID: data.sessionID

            });

            // Refresh the table with the updated semester list
            clear_Btn();
            populateSemesterTable(semestersData);

        })
        .catch(error => console.error('Error inserting semester:', error));
    }


// Function to populate the table
function populateSemesterTable(data) {
    const tableBody = document.querySelector("#semester_table_view tbody");
    tableBody.innerHTML = ""; // Clear existing rows

    data.forEach(semester => {
        const row = semesterCreateRow(semester);
        tableBody.appendChild(row);
    });
}

// Function to create a table row
function semesterCreateRow(semester) {
    const row = document.createElement("tr");

    // Semester ID cell
    const idCell = document.createElement("td");
    idCell.textContent = semester.id;
    row.appendChild(idCell);

    // Semester Title cell
    const titleCell = document.createElement("td");
    titleCell.textContent = semester.title;
    row.appendChild(titleCell);

    // Credit Hours cell
    const creditHoursCell = document.createElement("td");
    creditHoursCell.textContent = semester.creditHours;
    row.appendChild(creditHoursCell);

    // Section cell
    const sectionCell = document.createElement("td");
    sectionCell.textContent = semester.section;
    row.appendChild(sectionCell);

    // Capacity cell
    const capacityCell = document.createElement("td");
    capacityCell.textContent = semester.capacity;
    row.appendChild(capacityCell);

    return row;
}

// Function to handle sorting based on table header clicks
function handleHeaderClick() {

    const column = this.dataset.column; // Column to sort by
    const order = this.dataset.order; // Current sorting order

    let newOrder = 'asc';
    //    let arrow = ' &#9650;'; // Default up arrow
    //
    if (order === 'asc') {
        newOrder = 'desc';
        //arrow = ' &#9660;'; // Down arrow
    }
    //
    //    this.innerHTML = this.textContent + arrow;
    this.dataset.order = newOrder;

    // Sort data by column and order
    semestersData.sort((a, b) => {
        if (newOrder === 'asc') {
            return a[column] > b[column] ? 1 : -1;
        } else {
            return a[column] < b[column] ? 1 : -1;
        }
    });

    populateSemesterTable(semestersData); // Repopulate the table with sorted data
}

// Function to search through semestersData
function searchSemesterTable(value, data) {
    return data.filter(semester =>
        semester.title.toLowerCase().includes(value.toLowerCase())
    );
}

// Function to handle search input
function handleSemesterSearch() {

    const value = this.value;
    const filteredSemesters = searchSemesterTable(value, semestersData);
    populateSemesterTable(filteredSemesters);

}

semesterProgramSelect.addEventListener('change', handleSemesterComboFilter);
sessionSelect.addEventListener('change', handleSemesterComboFilter);
semesterTitleInput.addEventListener('change', handleSemesterComboFilter);
sectionInput.addEventListener('change', handleSemesterComboFilter);

function handleSemesterComboFilter() {


    let selectedProgram = null
    let selectedSession = null
    let selectedSemesterType = null;
    let selectedBatch = null;


    if (semesterProgramSelect.value != '-1') {

        selectedProgram = semesterProgramSelect.value;

    }
    if (sessionSelect.value != '-1') {

        selectedSession = sessionSelect.value;
    }
    if (semesterTitleInput.value > '0') {

        selectedSemesterType = semesterTitleInput.value;
    }
    if (sectionInput.value > '0') {

        selectedBatch = sectionInput.options[sectionInput.selectedIndex].text;

    }






    let filteredSemesters = semestersData;
    if (selectedProgram != null) {
        filteredSemesters = filteredSemesters.filter(semester => {
            return semester.programID == selectedProgram; // Added return statement here
        });
    }
    console.log("List: ", filteredSemesters);

    if (selectedBatch != null) {
        filteredSemesters = filteredSemesters.filter(semester => {

            return semester.section == selectedBatch

        }
        );
    }

    if (selectedSemesterType != null) {
        filteredSemesters = filteredSemesters.filter(semester => {

            return semester.semesterID == parseInt(selectedSemesterType);

        }
        );
    }

    if (selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester => {

            console.log("184")
            console.log(semester.sessionID, "==", semester.sessionID == selectedSession);

            return semester.sessionID == parseInt(selectedSession);
        }
        );
    }

    // Handle combinations of multiple filters (all filters will be applied step by step)

    if (selectedProgram != null && selectedBatch != null) {
        console.log("199")

        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.section == selectedBatch
        );
    }

    if (selectedProgram != null && selectedSemesterType != null) {
        console.log("200")

        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.semesterID == parseInt(selectedSemesterType)
        );
    }

    if (selectedProgram != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    if (selectedBatch != null && selectedSemesterType != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.section == selectedBatch &&
            semester.semesterID == parseInt(selectedSemesterType)
        );
    }

    if (selectedBatch != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.section == selectedBatch &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    if (selectedSemesterType != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.semesterID == parseInt(selectedSemesterType) &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    // Handle combinations of three filters

    if (selectedProgram != null && selectedBatch != null && selectedSemesterType != null) {

        console.log("247")

        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.section == selectedBatch &&
            semester.semesterID == parseInt(selectedSemesterType)
        );
    }

    if (selectedProgram != null && selectedBatch != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.section == selectedBatch &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    if (selectedProgram != null && selectedSemesterType != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.semesterID == parseInt(selectedSemesterType) &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    if (selectedBatch != null && selectedSemesterType != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.section == selectedBatch &&
            semester.semesterID == parseInt(selectedSemesterType) &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    // Handle all four filters

    if (selectedProgram != null && selectedBatch != null && selectedSemesterType != null && selectedSession != null) {
        filteredSemesters = filteredSemesters.filter(semester =>
            semester.programID == parseInt(selectedProgram) &&
            semester.section == selectedBatch &&
            semester.semesterID == parseInt(selectedSemesterType) &&
            semester.sessionID == parseInt(selectedSession)
        );
    }

    // Populate table with the filtered semestersData
    console.log(filteredSemesters);
    populateSemesterTable(filteredSemesters);

}



// Initialize event listeners for sorting, search, and table actions
function initializeSemesterEventListeners() {

    // Add event listeners to table headers
    const headers = document.querySelectorAll('th');
    headers.forEach(header => {
        header.addEventListener('click', handleHeaderClick);
    });

    // Add event listener to search input
    const searchInput = document.getElementById('search-input');
    searchInput.addEventListener('keyup', handleSemesterSearch);

    // Add event listeners for buttons
    const insertBtn = document.getElementById("add_semester_add_btn");
    insertBtn.addEventListener("click", handleInsert);

    const semesterUpdateBtn = document.getElementById("add_semester_update_btn");
    const semesterDeleteBtn = document.getElementById("add_semester_delete_btn");

    semesterUpdateBtn.addEventListener("click", handleUpdate);
    semesterDeleteBtn.addEventListener("click", handleDelete);

    // Hide update and delete buttons initially
    semesterUpdateBtn.style.visibility = "hidden";
    semesterDeleteBtn.style.visibility = "hidden";
}



// Handle update operation
function handleUpdate(event) {
    event.preventDefault(); // Prevent form submission

    // Get input values and trim any leading/trailing spaces
    const creditHours = semesterCreditHoursInput.value.trim();
    const capacity = capacityInput.value.trim();
    const section = sectionInput.options[sectionInput.selectedIndex].text.trim();
    const semesterTitle = semesterTitleInput.value;
    const programID = semesterProgramSelect.value.trim();
    const sessionID = sessionSelect.value.trim();

    const programTitle = semesterProgramSelect.options[semesterProgramSelect.selectedIndex].text.trim();
    const sTitle = semesterTitleInput.options[semesterTitleInput.selectedIndex].text.trim();
    const batch = sectionInput.value.trim();

    console.log("Batch: ", batch);
    const title = `${programTitle} ${sTitle}`;

    // Search for the matching semester in the array
    const foundSemester = semestersData.find(semester =>
        semester.title == title && semester.section == batch
    );

    // Validate input fields
    if (!semesterTitle || !creditHours || !section || !capacity || !programID || !sessionID) {
        alert("All fields must be filled out.");
        return;
    } else if (section <= 0 || capacity <= 0) {
        alert("Capacity and semester section must be positive.");
        return;
    }

    // Get the semester ID for the update (e.g., from a hidden field or selected row)
    const semesterID = semesterIDInput.value;

    // Create an updated semester object to send to the server
    const updatedSemester = {
        semesterTitle: title,
        semesterCreditHours: creditHours,
        section: batch,
        capacity: capacity,
        programID: programID,
        sID: semesterTitle,
        sessionID: sessionID
    };

    // Send PUT request to update the semester
    fetch(`/timetablexpert/semester-data/${semesterID}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedSemester)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update semester');
        }
        return response.json();
    })
    .then(data => {
        alert("Semester updated successfully!");

        // Update the local semestersData array with the updated semester data
        const semesterIndex = semestersData.findIndex(semester => semester.id == semesterID);
        if (semesterIndex !== -1) {
            semestersData[semesterIndex] = {
                id: semesterID,
                title: data.semesterTitle,
                creditHours: data.semesterCreditHours,
                section: (data.section == "1") ? "Morning" : "Replica",
                capacity: data.capacity,
                programID: data.programID,
                semesterID: data.sID,
                sessionID: data.sessionID
            };
        }

        // Refresh the table with the updated semester list
         updateSemesterInTable(semestersData);
        populateSemesterTable(semestersData);
        clear_Btn();


    })
    .catch(error => console.error('Error updating semester:', error));
}


// Update the semester in the local array and refresh the table
function updateSemesterInTable(updatedSemester) {
    const index = semestersData.findIndex(semester => semester.id === updatedSemester.semesterID);
    if (index !== -1) {
        semestersData[index].title = updatedSemester.semesterTitle;
        semestersData[index].creditHours = updatedSemester.semesterCreditHours;
        semestersData[index].section = updatedSemester.section;
        semestersData[index].capacity = updatedSemester.capacity;
        populateSemesterTable(semestersData); // Refresh table
    }
}

// Handle delete operation
function handleDelete(event) {
    event.preventDefault(); // Prevent form submission
    const semesterID = semesterIDInput.value.trim();

    if (!semesterID) {
        alert("Semester ID cannot be empty.");
        return;
    }

    fetch(`/timetablexpert/semester-data/${semesterID}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {


            return response.json();
        })
        .then(data => {
            alert("Semester deleted successfully!");
            removeSemesterFromTable(semesterID); // Remove semester from table
            clear_Btn();
        })
         .catch(error => {

            alert("The semester cannot be deleted because it is linked to other data. Please remove those connections first.");
            return console.error('Error deleting program:');

            });
}

// Remove the deleted semester from the local array and refresh the table
function removeSemesterFromTable(semesterID) {
    semestersData = semestersData.filter(semester => semester.id != Number(semesterID));
    populateSemesterTable(semestersData); // Refresh table
}

// Initialize table and event listeners on window load
window.onload = function () {
    populateSemesterTable(semestersData); // Populate table with initial data
    initializeSemesterEventListeners(); // Set up event listeners
    semesterFormCombo();

};

// Table interaction logic






let semesterSelectedRow = null; // To track selected row

//
semestersDataTable.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {


        semesterUpdateBtn.style.visibility = "visible";
        semesterDeleteBtn.style.visibility = "visible";
        const row = target.parentNode;

        if (semesterSelectedRow) {
            semesterSelectedRow.classList.remove("selected-row");
        }

        semesterSelectedRow = row;
        semesterSelectedRow.classList.add("selected-row");

        const cells = semesterSelectedRow.getElementsByTagName("td");


        // Populate the form inputs with the selected row's data
        semesterIDInput.value = cells[0].textContent;
        semesterCreditHoursInput.value = cells[2].textContent;
        capacityInput.value = cells[4].textContent;
    }

    event.stopPropagation();
});



// Semester Combo

function semesterFormCombo(){

   // Clear existing options and add the default placeholder option
    semesterProgramSelect.innerHTML = '<option value="-1" disabled selected>--Select Program--</option>';

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
                semesterProgramSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching data:', error));
}

//Session Combo
function semesterSessionCombo(){

 // Clear existing options and add the default placeholder option
    sessionSelect.innerHTML = '<option value="-1" disabled selected>--Select Session--</option>';

    // Fetch program data when the page is loaded
    fetch('/timetablexpert/session-data')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Log the data to verify the structure

            // Populate the select with new options from data.programData
            data.sessionData.forEach(session => {
                const option = document.createElement('option');
                option.value = session.sessionID; // Use unique identifier like programID
                option.textContent = session.title; // Display program name in the combo
                sessionSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching data:', error));

}



// Handle click outside table to clear selection
document.addEventListener("click", (event) => {

    const isClickInsideTable = semestersDataTable.contains(event.target);
    const isClickInsideInputs = document.querySelector('.form-wrapper').contains(event.target); // Adjust selector as needed



    // Only proceed to hide buttons if the click is outside the table, inputs, and combo box
    if (!isClickInsideTable && !isClickInsideInputs && semesterSelectedRow) {
        if (semesterSelectedRow) {
            semesterSelectedRow.classList.remove("selected-row");
        }
        semesterSelectedRow = null;


        const semesterUpdateBtn = document.getElementById("add_semester_update_btn");
        const semesterDeleteBtn = document.getElementById("add_semester_delete_btn");
        semesterUpdateBtn.style.visibility = "hidden";
        semesterDeleteBtn.style.visibility = "hidden";
        clear_Btn();
    }
});


function clear_Btn(){


  sectionInput.value = "-1";
      sectionInput.value = sectionInput.options[0].value;

    semesterTitleInput.value = "-1";
        semesterTitleInput.value = semesterTitleInput.options[0].value;

    semesterIDInput.value = "";
    semesterCreditHoursInput.value = "";
    capacityInput.value = "";
     semesterProgramSelect.value = "-1";
         semesterProgramSelect.value = semesterProgramSelect.options[0].value;

      sessionSelect.value = "-1";
          sessionSelect.value = sessionSelect.options[0].value;

          populateSemesterTable(semestersData);


}

semesterClearBtn.addEventListener("click", () => {

    clear_Btn();
    populateSemesterTable(semestersData);

})
