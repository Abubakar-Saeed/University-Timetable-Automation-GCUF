// Select elements
const teacherDataTable = document.getElementById("teacher_table_view");

const teacherIDField = document.getElementById('teacher_id_field');
const teacherNameField = document.getElementById('teacher_name_field');
const teacherPhoneField = document.getElementById('teacher_phone');
const teacherEmailField = document.getElementById('teacher_email');
const genderSelect = document.getElementById('gender_combo');
const departmentSelect = document.getElementById('teacher_program_combo');
const typeSelect = document.getElementById('type_combo');
const teacherTableBody = document.querySelector('#teacher_table_view tbody');
const teacherUpdateBtn = document.getElementById("add_teacher_update_btn");
const teacherDeleteBtn = document.getElementById("add_teacher_delete_btn");
const teacherClearBtn = document.getElementById("add_teacher_clear_btn");
const teacherAddBtn = document.getElementById("add_teacher_add_btn");
const searchInput = document.getElementById('search-input');


let teachers = [];

displayTeacherData();
function displayTeacherData() {

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
            teachers = data.teacherData.map(teacher => ({
                teacherID: teacher.teacherID,
                teacherName: teacher.teacherName,
                phoneNo: teacher.phoneNo,
                email: teacher.email,
                gender: teacher.gender,
                department: teacher.department,
                type: teacher.type,
            }));


            // Populate the table and initialize event listeners
            populateTeacherTable(teachers);
            initializeEventListeners();
        })
        .catch(error => console.error('Error fetching data:', error));


}
function populateTeacherTable(data) {

    teacherTableBody.innerHTML = ""; // Clear existing rows

    data.forEach(teacher => {
        const row = createTeacherRow(teacher);
        teacherTableBody.appendChild(row);
    });
}

// Function to create a table row for a teacher
function createTeacherRow(teacher) {
    const row = document.createElement("tr");

    const idCell = document.createElement("td");
    idCell.textContent = teacher.teacherID;
    row.appendChild(idCell);

    const nameCell = document.createElement("td");
    nameCell.textContent = teacher.teacherName;
    row.appendChild(nameCell);

    const phoneCell = document.createElement("td");
    phoneCell.textContent = teacher.phoneNo;
    row.appendChild(phoneCell);

    const emailCell = document.createElement("td");
    emailCell.textContent = teacher.email;
    row.appendChild(emailCell);

    const genderCell = document.createElement("td");
    genderCell.textContent = teacher.gender;
    row.appendChild(genderCell);

    const departmentCell = document.createElement("td");
    departmentCell.textContent = teacher.department;
    row.appendChild(departmentCell);

    const typeCell = document.createElement("td");
    typeCell.textContent = teacher.type;
    row.appendChild(typeCell);

    return row;
}

// Function to handle sorting based on table header clicks
function handleTeacherHeaderClick() {

    const column = this.dataset.column;
    const order = this.dataset.order;
    let newOrder = order === 'asc' ? 'desc' : 'asc';
    this.dataset.order = newOrder;

    teachers.sort((a, b) => {
        if (newOrder === 'asc') {
            return a[column] > b[column] ? 1 : -1;
        } else {
            return a[column] < b[column] ? 1 : -1;
        }
    });

    populateTeacherTable(teachers);
}

// Function to search through teachers
function searchTeacherTable(value, data) {

    return data.filter(teacher => {
        const searchValue = value.toLowerCase();

        // Check if the search value is included in any teacher property
        return (
            teacher.teacherName.toLowerCase().includes(searchValue) ||
            teacher.phoneNo.toLowerCase().includes(searchValue) ||
            teacher.email.toLowerCase().includes(searchValue) ||
            teacher.gender.toLowerCase().includes(searchValue) ||
            teacher.department.toLowerCase().includes(searchValue) ||
            teacher.type.toLowerCase().includes(searchValue)
        );
    });
}

// Function to handle search input for teachers
function handleTeacherSearch() {
    const value = this.value;
    const filteredTeachers = searchTeacherTable(value, teachers);
    populateTeacherTable(filteredTeachers);
}


// Initialize event listeners for sorting, search, and table actions
function initializeEventListeners() {
    departmentCombo();
    const headers = document.querySelectorAll('#teacher_table_view th');
    headers.forEach(header => {
        header.addEventListener('click', handleTeacherHeaderClick);
    });

    searchInput.addEventListener('keyup', handleTeacherSearch);

    teacherAddBtn.addEventListener("click", handleInsertTeacher);
    teacherUpdateBtn.addEventListener("click", handleUpdateTeacher);
   teacherDeleteBtn.addEventListener("click", handleDeleteTeacher);
       teacherClearBtn.addEventListener("click",clearTeacherFields);

    departmentSelect.addEventListener("change",handleTeacherComboFilter);
    genderSelect.addEventListener("change",handleTeacherComboFilter);
    typeSelect.addEventListener("change",handleTeacherComboFilter);


    teacherUpdateBtn.style.visibility = "hidden";
    teacherDeleteBtn.style.visibility = "hidden";
}

// Department Combo
function departmentCombo(){

    // Clear existing options and add the default placeholder option
        departmentSelect.innerHTML = '<option value="-1" disabled selected>-- Select Program --</option>';

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
                    departmentSelect.appendChild(option);
                });
            })

            .catch(error => console.error('Error fetching data:', error));
}

function handleTeacherComboFilter() {

    let filteredTeachers = teachers; // Start with all teachers

    // Get selected values from the dropdowns
    const selectedGender = genderSelect.options[genderSelect.selectedIndex]?.text;
    let selectedGenderIndex = genderSelect.value;
    const selectedDepartment = departmentSelect.options[departmentSelect.selectedIndex]?.text;
    let selectedDepartmentIndex = departmentSelect.value;
    const selectedType = typeSelect.options[typeSelect.selectedIndex]?.text;
    let selectedTypeIndex = typeSelect.value;

    // Apply filters based on all possible pair and single selections
    if (selectedGenderIndex !== "-1" && selectedDepartmentIndex !== "-1" && selectedTypeIndex !== "-1") {
        // All three filters are selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.gender === selectedGender &&
            teacher.department === selectedDepartment &&
            teacher.type === selectedType
        );
    } else if (selectedGenderIndex !== "-1" && selectedDepartmentIndex !== "-1") {
        // Gender and Department are selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.gender === selectedGender &&
            teacher.department === selectedDepartment
        );
    } else if (selectedGenderIndex !== "-1" && selectedTypeIndex !== "-1") {
        // Gender and Type are selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.gender === selectedGender &&
            teacher.type === selectedType
        );
    } else if (selectedDepartmentIndex !== "-1" && selectedTypeIndex !== "-1") {
        // Department and Type are selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.department === selectedDepartment &&
            teacher.type === selectedType
        );
    } else if (selectedGenderIndex !== "-1") {
        // Only Gender is selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.gender === selectedGender
        );
    } else if (selectedDepartmentIndex !== "-1") {
        // Only Department is selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.department === selectedDepartment
        );
    } else if (selectedTypeIndex !== "-1") {
        // Only Type is selected
        filteredTeachers = filteredTeachers.filter(teacher =>
            teacher.type === selectedType
        );
    }

    // Populate the teacher table with filtered results
    populateTeacherTable(filteredTeachers);

}
function clearTeacherFields() {

    teacherIDField.value = "";
    teacherNameField.value = "";
    teacherPhoneField.value = "";
    teacherEmailField.value = "";
    genderSelect.value = "-1";
    departmentSelect.value = "-1";
    typeSelect.value = "-1";

    // Repopulate the teacher table with all data after clearing fields
    populateTeacherTable(teachers);
}
function  handleInsertTeacher(){

    // Collect form values
    const teacherID = teacherIDField.value;
    const teacherName = teacherNameField.value;
    const phoneNo = teacherPhoneField.value;
    const email = teacherEmailField.value;
    const gender = genderSelect.value;
    const department = departmentSelect.options[departmentSelect.selectedIndex].text;

    const type = typeSelect.value;


      // Check if any field is empty
        if (!teacherName || !phoneNo || !email || !gender || !department || !type) {
            alert('Please fill in all the fields.');
            return; // Prevent form submission if any field is empty
        }
         // Search for the teacher in the local teachers array
        const teacherExists = teachers.find(teacher => teacher.teacherName.toLowerCase() === teacherName.toLowerCase());

        // If teacher exists, show an error alert
       if (teacherExists) {
               alert("Teacher Name: " + teacherName + " already exists.");
               return;
}

    // Create teacher object
    const newTeacher = {
        teacherName: teacherName,
        phoneNo: phoneNo,
        email: email,
        gender: gender,
        department: department,
        type: type
    };

    // Send data to server via POST request
    fetch('/timetablexpert/teacher-data', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(newTeacher)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to add teacher');
        }
        return response.json();
    })
    .then(data => {

        let gen ;
        let ty;
        if (data.gender == "1"){

            gen = "Female"

        }else{

                    gen = "Male";

        }
        if (data.type == "0"){

                ty = "Regular";
        }else{

                ty = "Visiting";
        }
        // Refresh table after successful insertion
        teachers.push({
            teacherID: data.teacherID,
            teacherName: data.teacherName,
            phoneNo: data.phoneNo,
            email: data.email,
            gender: gen,
            department: data.department,
            type: ty
        });
        clearTeacherFields();

        alert('Teacher added successfully:', data);
    })
    .catch(error => console.error('Error adding teacher:', error));

}
function handleUpdateTeacher() {
    // Collect form values
    const teacherID = teacherIDField.value;
    const teacherName = teacherNameField.value;
    const phoneNo = teacherPhoneField.value;
    const email = teacherEmailField.value;
    const gender = genderSelect.value;
    const department = departmentSelect.options[departmentSelect.selectedIndex].text;
    const type = typeSelect.value;

    // Check if any field is empty
    if (!teacherID || !teacherName || !phoneNo || !email || !gender || !department || !type) {
        alert('Please fill in all the fields.');
        return; // Prevent update if any field is empty
    }


    // Check if the teacher exists in the local teachers array
    const teacherIndex = teachers.findIndex(teacher => {

        console.log(teacher.teacherID, " == ", teacherID , ": ",teacher.teacherID == teacherID);
       return teacher.teacherID == teacherID;
    });

    if (teacherIndex === -1) {
        alert("Teacher with ID: " + teacherID + " does not exist.");
        return;
    }

    // Create an updated teacher object
    const updatedTeacher = {
        teacherID: teacherID,
        teacherName: teacherName,
        phoneNo: phoneNo,
        email: email,
        gender: gender,
        department: department,
        type: type
    };

    // Send updated data to server via PUT request
    fetch(`/timetablexpert/teacher-data/${teacherID}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedTeacher)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update teacher');
        }
        return response.json();
    })
    .then(data => {
        // Update teacher in the local array
        let gen = data.gender === "1" ? "Female" : "Male";
        let ty = data.type === "0" ? "Regular" : "Visiting";

        teachers[teacherIndex] = {
            teacherID: data.teacherID,
            teacherName: data.teacherName,
            phoneNo: data.phoneNo,
            email: data.email,
            gender: gen,
            department: data.department,
            type: ty
        };

        clearTeacherFields();

        alert('Teacher updated successfully:', data);
    })
    .catch(error => console.error('Error updating teacher:', error));
}
function handleDeleteTeacher() {
    // Get the teacher ID from the form or selection
    const teacherID = teacherIDField.value;

    // Check if teacherID is provided
    if (!teacherID) {
        alert('Please enter the Teacher ID to delete.');
        return;
    }

    // Find index of the teacher in the local teachers array
    const teacherIndex = teachers.findIndex(teacher => teacher.teacherID == teacherID);
    if (teacherIndex === -1) {
        alert("Teacher with ID: " + teacherID + " does not exist.");
        return;
    }

    // Confirm deletion action
    if (!confirm("Are you sure you want to delete this teacher?")) {
        return;
    }

    // Send DELETE request to the server
    fetch(`/timetablexpert/teacher-data/${teacherID}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete teacher');
        }
        return response.json();
    })
    .then(data => {
        // Remove teacher from local array
        teachers.splice(teacherIndex, 1);

        // Clear form fields and show success message
        clearTeacherFields();
        alert('Teacher deleted successfully.');
    })
    .catch(error => console.error('Error deleting teacher:', error));
}



let teacherSelectedRow = null; // To track selected row

//
teacherDataTable.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {


        teacherUpdateBtn.style.visibility = "visible";
        teacherDeleteBtn.style.visibility = "visible";
        const row = target.parentNode;

        if (teacherSelectedRow) {
            teacherSelectedRow.classList.remove("selected-row");
        }

        teacherSelectedRow = row;
        teacherSelectedRow.classList.add("selected-row");

        const cells = teacherSelectedRow.getElementsByTagName("td");


        // Populate the form inputs with the selected row's data
        teacherIDField.value = cells[0].textContent;
        teacherNameField.value = cells[1].textContent;
        teacherPhoneField.value = cells[2].textContent;
        teacherEmailField.value = cells[3].textContent;
    }

    event.stopPropagation();
});


// Handle click outside table to clear selection
document.addEventListener("click", (event) => {

    const isClickInsideTable = teacherDataTable.contains(event.target);
    const isClickInsideInputs = document.querySelector('.form-wrapper-teacher').contains(event.target); // Adjust selector as needed



    // Only proceed to hide buttons if the click is outside the table, inputs, and combo box
    if (!isClickInsideTable && !isClickInsideInputs && teacherSelectedRow) {
        if (teacherSelectedRow) {
            teacherSelectedRow.classList.remove("selected-row");
        }
        teacherSelectedRow = null;

        teacherUpdateBtn.style.visibility = "hidden";
        teacherDeleteBtn.style.visibility = "hidden";
        clearTeacherFields();
    }
});





