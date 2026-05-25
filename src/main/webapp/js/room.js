let roomData = [];
let labData = [];

const tableBody = document.querySelector("#room_table_view tbody");
const headerRow = document.querySelector("#room_table_view thead tr");
const selectType = document.getElementById("room_type_combo");
const roomTableView = document.getElementById("room_table_view");
const roomIDField = document.getElementById('room_id_field');
const roomNoField = document.getElementById('roomNo');
const capacityField = document.getElementById('capacityField');
const label = document.getElementById('room_label');
const typeCombo = document.getElementById('room_type_combo');
const programRoomCombo = document.getElementById('room_program_combo');
const clearRoomBtn = document.getElementById("add_room_clear_btn");
const roomDeleteBtn = document.getElementById("add_room_delete_btn");
const roomUpdateBtn = document.getElementById("add_room_update_btn");
const roomAddBtn = document.getElementById("add_room_add_btn");
const roomSearchInput = document.getElementById('search-input');



// Fetch data from the server on page load
function fetchData() {
    fetch('/timetablexpert/room-data')
        .then(response => response.json())
        .then(data => {
            roomData = data.roomData.map(room => ({
                roomID: room.roomID,
                roomNo: room.roomNo,
                capacity: room.capacity,
                program: room.program,
                programID: room.programID,
                type: room.type
            }));

            labData = data.labData.map(room => ({

                roomID: room.roomID,
                roomNo: room.roomNo,
                capacity: room.capacity,
                program: room.program,
                programID: room.programID,
                type: room.type

            }));

            // Default to showing roomData
            populateRoomTable(roomData, "Room ID", "Room No");
            initializeRoomEventListener();
        })
        .catch(error => console.error("Error fetching data:", error));
}

function populateRoomTable(dataArray, idLabel, roomLabel) {
    // Update header row labels based on the selected type
    headerRow.innerHTML = `
        <th>${idLabel}</th>
        <th>${roomLabel}</th>
        <th>Capacity</th>
        <th>Department</th>
    `;

    // Clear any existing rows in the table body
    tableBody.innerHTML = '';

    // Populate table body with new rows based on dataArray
    dataArray.forEach(item => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${item.roomID}</td>
            <td>${item.roomNo}</td>
            <td>${item.capacity}</td>
            <td>${item.program}</td>
        `;
        tableBody.appendChild(row);
    });
}


function searchRoomTable(value, data) {
    const searchValue = value.toLowerCase();

    // Filter rooms that match the search value in any of the room properties
    return data.filter(room => (
        room.roomNo.toLowerCase().includes(searchValue) ||
        room.capacity.toString().includes(searchValue) ||
        room.program.toLowerCase().includes(searchValue)
    ));
}

// Function to handle search input for rooms
function handleRoomSearch() {
    const value = this.value; // Get the search input value

    if (selectType.value == "1"){
        const filteredRooms = searchRoomTable(value, roomData); // Assuming roomData holds all room information

        populateRoomTable(filteredRooms, "Room ID", "Room No"); // Repopulate table with filtered data

    }else if (selectType.value == "2"){
        const filteredRooms = searchRoomTable(value, labData); // Assuming roomData holds all room information

        populateRoomTable(filteredRooms, "Lab ID", "Lab No"); // Repopulate table with filtered data

    }else{

     const filteredRooms = searchRoomTable(value, roomData); // Assuming roomData holds all room information

            populateRoomTable(filteredRooms, "Room ID", "Room No");
    }
}


function handleRoomInsert(event) {
    event.preventDefault(); // Prevent form submission

    // Get input values and trim any leading/trailing spaces
    const roomNo = roomNoField.value.trim();
    const capacity = capacityField.value.trim();
    const program = programRoomCombo[programRoomCombo.selectedIndex].textContent;

    const programID =  programRoomCombo.value.trim();
    const roomType = parseInt(typeCombo.value.trim(), 10);

    // Validate input fields
        console.log("Program: ", programID)

    if (!roomNo || !capacity || !program || !programID || !roomType) {
        alert("All fields must be filled out.");
        return;
    } else if (capacity <= 0) {
        alert("Capacity must be a positive number.");
        return;
    }

    console.log("Room Type: ", roomType);

    if (roomType == 1){
     let existingEntry = roomData.find(item => item.roomNo == roomNo && item.program == program);

        // If an entry is found, display an error message
        if (existingEntry) {
            alert(`${roomNo}" for program "${program}" already exists.`);
            return ; // Indicate that the room/lab already exists
        }
     }else if (roomType == 2){

      let existingEntry = labData.find(item => item.roomNo == roomNo && item.program == program);

             // If an entry is found, display an error message
             if (existingEntry) {
                 alert(`${roomNo}" for program "${program}" already exists.`);
                 return ;
             }

     }


    // Create a new room object to send to the server
    const newRoom = {
        roomNo: roomNo,
        capacity: capacity,
        program: program,
        programID: programID,
        type: roomType
    };

        console.log("New Room: ", newRoom);
    // Send POST request to insert the new room
    fetch('/timetablexpert/room-data', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newRoom)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to insert room');
        }
        return response.json();
    })
    .then(data => {
        alert("Room added successfully!");

        // Add the new room to the local rooms array (or update the view)


         if (roomType == 1){


            roomData.push({
                     roomID: data.roomID,
                     roomNo: data.roomNo,
                     capacity: data.capacity,
                     program: data.program,
                     programID: data.programID,
                     type: data.type
                 });


                        populateRoomTable(roomData);

             }else if (roomType == 2){

   labData.push({
            roomID: data.roomID,
            roomNo: data.roomNo,
            capacity: data.capacity,
            program: data.program,
            programID: data.programID,
            type: data.type
        });

                     populateRoomTable(labData);


             }
        // Refresh the table with the updated room list

        // Clear the input fields
        clearRoomFields();
    })
    .catch(error => console.error('Error inserting room:', error));
}
function handleRoomUpdate(event) {
    event.preventDefault(); // Prevent form submission

    // Get input values and trim any leading/trailing spaces
    const roomNo = roomNoField.value.trim();
    const capacity = capacityField.value.trim();
    const program = programRoomCombo[programRoomCombo.selectedIndex].textContent;

    const programID = programRoomCombo.value.trim();
    const roomType = parseInt(typeCombo.value.trim(), 10);
    const roomID = roomIDField.value.trim(); // Assuming there is a hidden field or input for room ID

    // Validate input fields
    console.log("Program: ", programID);

    if (!roomID || !roomNo || !capacity || !program || !programID || !roomType) {
        alert("All fields must be filled out.");
        return;
    } else if (capacity <= 0) {
        alert("Capacity must be a positive number.");
        return;
    }

    console.log("Room Type: ", roomType);

    // Check for duplicate entries within the same program
    if (roomType == 1) {
        let existingEntry = roomData.find(
            item => item.roomNo === roomNo && item.program === program && item.roomID !== roomID
        );

        if (existingEntry) {
            alert(`Room "${roomNo}" for program "${program}" already exists.`);
            return; // Stop execution if a duplicate is found
        }
    } else if (roomType == 2) {
        let existingEntry = labData.find(
            item => item.roomNo === roomNo && item.program === program && item.roomID !== roomID
        );

        if (existingEntry) {
            alert(`Lab "${roomNo}" for program "${program}" already exists.`);
            return; // Stop execution if a duplicate is found
        }
    }

    // Create the updated room object
    const updatedRoom = {
        roomID: roomID,
        roomNo: roomNo,
        capacity: capacity,
        program: program,
        programID: programID,
        type: roomType
    };

    console.log("Updated Room: ", updatedRoom);

    // Send PUT request to update the room
    fetch(`/timetablexpert/room-data/${roomID}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedRoom)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update room');
        }
        return response.json();
    })
    .then(data => {
        alert("Room updated successfully!");

        // Update the local room data array
        const roomIndex = roomData.findIndex(item => item.roomID == roomID);
        const labIndex = labData.findIndex(item => item.roomID == roomID);
        if (roomIndex !== -1) {
            roomData[roomIndex] = { ...roomData[roomIndex], ...updatedRoom };
        }
         if (labIndex !== -1) {
                    labData[labIndex] = { ...labData[labIndex], ...updatedRoom };
                }


        // Refresh the table with the updated room list
        populateRoomTable(roomData, "Room ID");

        // Clear the input fields
        clearRoomFields();
    })
    .catch(error => console.error('Error updating room:', error));
}
function handleRoomDelete(event) {
    event.preventDefault();

    const roomID = roomIDField.value.trim(); // Retrieve the room ID from the input field
    const roomType = parseInt(typeCombo.value.trim(), 10); // Retrieve the room type (1 for room, 2 for lab)

    // Validate room ID and type
    if (!roomID || !roomType) {
        alert("Room ID and Type are required.");
        return;
    }

    // Confirm the deletion with the user
    if (!confirm("Are you sure you want to delete this room?")) {
        return; // Exit if the user cancels
    }

    // Construct the request body
    const requestBody = {
        type: roomType // Pass the room type in the body
    };

    // Send DELETE request to the server
    fetch(`/timetablexpert/room-data/${roomID}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody) // Send the room type in the request body
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete room');
        }
        return response.json();
    })
    .then(data => {
        alert("Room deleted successfully!");

        // Remove the deleted room from the local data array
        roomData = roomData.filter(item => item.roomID != roomID);
        labData = labData.filter(item => item.roomID != roomID);



        // Refresh the table with the updated room list
        populateRoomTable(roomData, "Room ID");

        // Clear the input fields
        clearRoomFields();
    })
    .catch(error => console.error('Error deleting room:', error));
}



// Department Combo

function roomDepartmentCombo(){


  // Clear existing options and add the default placeholder option
    programRoomCombo.innerHTML = '<option value="-1" disabled selected>-- Select Program --</option>';

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
                programRoomCombo.appendChild(option);
            });
        })

        .catch(error => console.error('Error fetching data:', error));
}

// Function to filter the rooms based on selected program
function handleRoomComboFilter() {


    const selectedProgram = programRoomCombo[programRoomCombo.selectedIndex].textContent; // Get the selected program value

    // Filter the data based on the selected program
    let filteredData = [];
    if (selectType.value == "1") {
        filteredData = roomData.filter(room => room.program == selectedProgram);

    } else if (selectType.value == "2") {
        // Filter labs data based on selected program
        filteredData = labData.filter(room => room.program == selectedProgram );
    }

    // Repopulate the table with filtered data
    if (selectType.value == "1") {
        populateRoomTable(filteredData, "Room ID", "Room Name");
    } else if (selectType.value == "2") {
        populateRoomTable(filteredData, "Lab ID", "Lab Name");
    }
}

function initializeRoomEventListener() {

    roomDepartmentCombo();

    // Attach the handleData function to the "change" event on type_combo

    roomSearchInput.addEventListener("input", handleRoomSearch);

    programRoomCombo.addEventListener('change',handleRoomComboFilter);
    roomAddBtn.addEventListener('click',handleRoomInsert);
    roomUpdateBtn.addEventListener('click',handleRoomUpdate);
    roomDeleteBtn.addEventListener('click',handleRoomDelete);
    clearRoomBtn.addEventListener('click',clearRoomFields);
    selectType.addEventListener("change", ()=>{

    const selectedType = selectType.value;

        console.log("Type: ", selectedType);
        if (selectedType == "1") {
            populateRoomTable(roomData, "Room ID", "Room Name");
            label.innerText = "Room Name";
        } else if (selectedType == "2") {
             label.innerText = "Lab Name";
            populateRoomTable(labData, "Lab ID", "Lab Name");
        }

    });
}
function clearRoomFields() {

    // Clear the room-related fields
    roomIDField.value = "";  // Room ID field (hidden)
    roomNoField.value = "";  // Room No input field
    capacityField.value = ""; // Capacity input field
    typeCombo.value = "1";  // Reset Room Type dropdown
    programRoomCombo.value = "-1";  // Reset Program dropdown
    label.innerText = "Room ID";
    // Repopulate the room table with all room data after clearing fields
    populateRoomTable(roomData, "Room ID", "Room No"); // Assuming 'roomData' is an array holding room data
}




// Initialize fetching of data
fetchData();

let roomSelectedRow = null; // To track selected row

//
room_table_view.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {

        roomUpdateBtn.style.visibility = "visible";
        roomDeleteBtn.style.visibility = "visible";
        const row = target.parentNode;

        if (roomSelectedRow) {
            roomSelectedRow.classList.remove("selected-row");
        }

        roomSelectedRow = row;
        roomSelectedRow.classList.add("selected-row");

        const cells = roomSelectedRow.getElementsByTagName("td");


        // Populate the form inputs with the selected row's data
        roomIDField.value = cells[0].textContent;
        roomNoField.value = cells[1].textContent;
        capacityField.value = cells[2].textContent;
    }

    event.stopPropagation();
});


// Handle click outside table to clear selection
document.addEventListener("click", (event) => {

    const isClickInsideTable = roomTableView.contains(event.target);
    const isClickInsideInputs = document.querySelector('.form-wrapper-room').contains(event.target); // Adjust selector as needed



    // Only proceed to hide buttons if the click is outside the table, inputs, and combo box
    if (!isClickInsideTable && !isClickInsideInputs && roomSelectedRow) {
        if (roomSelectedRow) {
            roomSelectedRow.classList.remove("selected-row");
        }
        roomSelectedRow = null;

        roomUpdateBtn.style.visibility = "hidden";
        roomDeleteBtn.style.visibility = "hidden";
        clearRoomFields();
    }
});
