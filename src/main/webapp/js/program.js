let programs = [];

// Fetch the program data from the server

document.addEventListener("DOMContentLoaded", function() {

    programSelectCombo();
});

function programSelectCombo(){


fetch('/timetablexpert/program-data')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {

        console.log("Data: ",data);
        programs = data.programData.map(program => ({
            id: program.programID,
            name: program.programName
        }));

        console.log("Program: ",programs);
;        // Populate the table and initialize event listeners
        populateProgramTable(programs);
        initializeProgramEventListeners();
    })
    .catch(error => console.error('Error fetching data:', error));
}


// Function to populate the table
function populateProgramTable(data) {
    const tableBody = document.querySelector("#program_table_view tbody");
    tableBody.innerHTML = ""; // Clear existing rows

    data.forEach(program => {
        const row = createRow(program);
        tableBody.appendChild(row);
    });
}


// Function to create a table row
function createRow(program) {
    const row = document.createElement("tr");

    // Program ID cell
    const idCell = document.createElement("td");
    idCell.textContent = program.programID;
    row.appendChild(idCell);

    // Program Name cell
    const nameCell = document.createElement("td");
    nameCell.textContent = program.programName;
    row.appendChild(nameCell);

    return row;
}

function handleHeaderClick() {
    const column = this.dataset.column; // Column to sort by
       const order = this.dataset.order; // Current sorting order

       let newOrder = 'asc';
   //    let arrow = ' &#9650;'; // Default up arrow
   //
       if (order === 'asc') {
           newOrder = 'desc';
          // arrow = ' &#9660;'; // Down arrow
       }
   //
   //
   //    this.innerHTML = this.textContent + arrow;


    this.dataset.order = newOrder; // Update the order data attribute

    // Sort the programs array based on the column and order
    programs.sort((a, b) => {
        if (newOrder === 'asc') {
            return a[column] > b[column] ? 1 : -1;
        } else {
            return a[column] < b[column] ? 1 : -1;
        }
    });

    populateProgramTable(programs); // Refresh the table with sorted data
}
// Function to search through programs
function searchProgramTable(value, data) {
    return data.filter(program =>
        program.name.toLowerCase().includes(value.toLowerCase())
    );
}

// Function to handle search input
function handleProgramSearch() {
    const value = this.value;
    const filteredPrograms = searchProgramTable(value, programs);
    populateProgramTable(filteredPrograms);
}

// Initialize event listeners for sorting and search
function initializeProgramEventListeners() {
    // Add event listeners to table headers
    const headers = document.querySelectorAll('th');
    headers.forEach(header => {
        header.addEventListener('click', handleHeaderClick);
    });

    // Add event listener to search input
    const searchInput = document.getElementById('search-input');
    searchInput.addEventListener('keyup', handleProgramSearch);

    // Add event listener for insert button
    const insertBtn = document.getElementById("add_program_button");
    insertBtn.addEventListener("click", handleProgramInsert);

    // Add event listener for update button
    const updateBtn = document.getElementById("program_update_button");
    updateBtn.addEventListener("click", handleProgramUpdate);

    // Add event listener for delete button
    const deleteBtn = document.getElementById("program_delete_button");
    deleteBtn.addEventListener("click", handleProgramDelete);
}

// Handle insert operation
function handleProgramInsert(event) {
    event.preventDefault(); // Prevent form submission
    const programName = document.getElementById("programName").value.trim();


    if (!programName) {
        alert("Program name cannot be empty.");
        return;
    }

  let matchProgram = programs.find(program =>
   program.name == programName

  );



  if (matchProgram) {
      alert("Program already exists.");
     return;
  }
    const newProgram = { programName };


    fetch('/timetablexpert/program-data', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newProgram)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to insert program');
        }
        return response.json();
    })
    .then(data => {
        alert("Program inserted successfully!");
        programs.push({ id: data.programID, name: data.programName }); // Add new program to local array
        populateProgramTable(programs); // Refresh table
    })
    .catch(error => console.error('Error inserting program:', error));
}

// Handle update operation
function handleProgramUpdate(event) {
    event.preventDefault(); // Prevent form submission
    const programID = document.getElementById("programID").value.trim();
    const programName = document.getElementById("programName").value.trim();

    if (!programID || !programName) {
        alert("Program ID and name cannot be empty.");
        return;
    }

    const updatedProgram = {
        programID,
        programName
    };

    fetch(`/timetablexpert/program-data/${programID}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedProgram)
    })
    .then(response => {

        return response.json();
    })
    .then(data => {



        if (!data.success){

            alert(data.message);

        }
          alert("Program updated successfully!");
          updateProgramInTable(data);
    })

    .catch(error => console.error('Error updating program:', error));
}

// Update the program in the local array and refresh the table
function updateProgramInTable(updatedProgram) {
    const index = programs.findIndex(program => program.id === updatedProgram.programID);
    if (index !== -1) {
        programs[index].name = updatedProgram.programName; // Update program name
        populateProgramTable(programs); // Refresh table
    }
}

// Handle delete operation
function handleProgramDelete(event) {
    event.preventDefault(); // Prevent form submission
    const programID = document.getElementById("programID").value;

    if (!programID) {
        alert("Program ID cannot be empty.");
        return;
    }

    fetch(`/timetablexpert/program-data/${programID}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => {
//        if (!response.ok) {
//            throw new Error('Failed to delete program');
//        }
        return response.json();
    })
    .then(data => {

        alert("Program deleted successfully!");
        removeProgramFromTable(programID); // Remove program from table


    })
    .catch(error => {

    alert("The program cannot be deleted because it is linked to other data. Please remove those connections first.");
    return console.error('Error deleting program:');

    });
    
}


// Remove the deleted program from the local array and refresh the table
function removeProgramFromTable(programID) {



        // Filter out the deleted program
        programs = programs.filter(program => {

        console.log(program.id,"==",programID,": ",program.id != programID);
        return program.id != programID;

        });


        populateProgramTable(programs); // Refresh table


}

// Initialize table and event listeners on window load
window.onload = function() {
    populateProgramTable(programs); // Populate table with initial data
    initializeProgramEventListeners(); // Set up event listeners
};

// Table interaction logic
const dataTable = document.getElementById("program_table_view");
const programIDInput = document.getElementById("programID");
const programNameInput = document.getElementById("programName");

let selectedRow = null; // To track selected row





dataTable.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {

        const updateBtn = document.getElementById("program_update_button");
        const deleteBtn = document.getElementById("program_delete_button");

       updateBtn.style.visibility = "visible";
       deleteBtn.style.visibility = "visible";
        const row = target.parentNode;

        if (selectedRow) {
            selectedRow.classList.remove("selected-row");
        }

        selectedRow = row;
        selectedRow.classList.add("selected-row");

        const programID = row.cells[0].textContent;
        const programName = row.cells[1].textContent;

        // Populate the form inputs with the selected row's data
        programIDInput.value = programID;
        programNameInput.value = programName;
    }

    event.stopPropagation();
});

// Handle click outside table to clear selection
document.addEventListener("click", (event) => {

    const isClickInsideTable = dataTable.contains(event.target);
    const isClickInsideInputs = programIDInput.contains(event.target) || programNameInput.contains(event.target);

    if (!isClickInsideTable && !isClickInsideInputs && selectedRow) {
        const updateBtn = document.getElementById("program_update_button");
        const deleteBtn = document.getElementById("program_delete_button");
        selectedRow.classList.remove("selected-row");
        selectedRow = null;

        programIDInput.value = "";
        programNameInput.value = "";
        updateBtn.style.visibility = "hidden";
        deleteBtn.style.visibility = "hidden";
    }
});