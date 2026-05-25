let sessions = [];

// Fetch the Session data from the server
fetch('/timetablexpert/session-data')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {

        // Store fetched Session data
        sessions = data.sessionData.map(session => ({
            id: session.sessionID,
            name: session.title
        }));

        console.log(data.sessionData);

        // Populate the table and initialize event listeners
        populateSessionTable(sessions);
        initializeSessionEventListeners();
    })
    .catch(error => console.error('Error fetching data:', error));

// Function to populate the table
function populateSessionTable(data) {

    const tableBody = document.querySelector("#session_table_view tbody");
    tableBody.innerHTML = ""; // Clear existing rows

    data.forEach(session => {
        const row = createRow(session);
        tableBody.appendChild(row);
    });
}

// Function to create a table row
function createRow(session) {
    const row = document.createElement("tr");

    // Session ID cell
    const idCell = document.createElement("td");
    idCell.textContent = session.id;
    row.appendChild(idCell);

    // Session Name cell
    const nameCell = document.createElement("td");
    nameCell.textContent = session.name;
    row.appendChild(nameCell);

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
       // arrow = ' &#9660;'; // Down arrow
    }
//
//
//    this.innerHTML = this.textContent + arrow;

this.dataset.order = newOrder;
    // Sort data by column and order
    sessions.sort((a, b) => {
        if (newOrder === 'asc') {
            return a[column] > b[column] ? 1 : -1;
        } else {
            return a[column] < b[column] ? 1 : -1;
        }
    });

    populateSessionTable(sessions); // Repopulate the table with sorted data
}

// Function to search through sessions
function searchSessionTable(value, data) {
    return data.filter(session =>
        session.name.toLowerCase().includes(value.toLowerCase())
    );
}

// Function to handle search input
function handleSessionSearch() {
    const value = this.value;
    const filteredsessions = searchSessionTable(value, sessions);
    populateSessionTable(filteredsessions);
}

// Initialize event listeners for sorting and search
function initializeSessionEventListeners() {
    // Add event listeners to table headers
    const headers = document.querySelectorAll('th');
    headers.forEach(header => {
        header.addEventListener('click', handleHeaderClick);
    });

    // Add event listener to search input
    const searchInput = document.getElementById('search-input');
    searchInput.addEventListener('keyup', handleSessionSearch);

    // Add event listener for insert button
    const insertBtn = document.getElementById("add_session_button");
    insertBtn.addEventListener("click", handleSessionInsert);

    // Add event listener for update button
    const updateBtn = document.getElementById("session_update_button");
    updateBtn.addEventListener("click", handleSessionUpdate);

    // Add event listener for delete button
    const deleteBtn = document.getElementById("session_delete_button");
    deleteBtn.addEventListener("click", handleSessionDelete);
}

// Handle insert operation
function handleSessionInsert(event) {
    event.preventDefault(); // Prevent form submission
    const sessionName = document.getElementById("sessionName").value.trim();

    if (!sessionName) {
        alert("session name cannot be empty.");
        return;
    }
     let matchSession = sessions.find(session =>
       session.name == sessionName

      );



      if (matchSession) {
          alert("Session already exists.");
         return;
      }

    const newSession = { title: sessionName  };

    console.log(newSession);
    fetch('/timetablexpert/session-data', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newSession)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to insert Session');
        }
        return response.json();
    })
    .then(data => {
        alert("Session inserted successfully!");
        sessions.push({ id: data.sessionID, name: data.title }); // Add new Session to local array
        populateSessionTable(sessions); // Refresh table
    })
    .catch(error => console.error('Error inserting Session:', error));
}

// Handle update operation
function handleSessionUpdate(event) {

    event.preventDefault(); // Prevent form submission
    const sessionID = document.getElementById("sessionID").value.trim();
    const sessionName = document.getElementById("sessionName").value.trim();

    if (!sessionID || !sessionName) {
        alert("Session ID and name cannot be empty.");
        return;
    }

    const updatedSession = {
        sessionID,
        title: sessionName
    };

    fetch(`/timetablexpert/session-data/${sessionID}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedSession)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update Session');
        }
        return response.json();
    })
    .then(data => {
        alert("Session updated successfully!");
        updateSessionInTable(data); // Update the table with new data
    })
    .catch(error => console.error('Error updating Session:', error));
}

// Update the Session in the local array and refresh the table
function updateSessionInTable(updatedSession) {

    const index = sessions.findIndex(Session => Session.id === Number(updatedSession.sessionID));
    if (index !== -1) {
        sessions[index].name = updatedSession.title; // Update Session name
        populateSessionTable(sessions); // Refresh table
    }
}

// Handle delete operation
function handleSessionDelete(event) {
    event.preventDefault(); // Prevent form submission
    const sessionID = document.getElementById("sessionID").value.trim();

    if (!sessionID) {

        alert("Session ID cannot be empty.");
        return;

    }

    fetch(`/timetablexpert/session-data/${sessionID}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete Session');
        }
        return response.json();
    })
    .then(data => {
        alert("Session deleted successfully!");
        removeSessionFromTable(sessionID); // Remove Session from table
    })
    .catch(error => console.error('Error deleting Session:', error));
}


// Remove the deleted Session from the local array and refresh the table
function removeSessionFromTable(sessionID) {

    // Remove the deleted Session from the local array and refresh the table
        console.log("Trying to delete Session ID:", sessionID); // Log the ID to be deleted

        // Log all existing Session IDs before filtering
        console.log("Existing Session IDs before filtering:", sessions.map(Session => Session.id));

        // Filter out the deleted Session
        sessions = sessions.filter(Session => Session.id !== Number(sessionID));

        // Log remaining Session IDs after filtering
        console.log("Remaining Session IDs after filtering:", sessions.map(Session => Session.id));

        populateSessionTable(sessions); // Refresh table


}

// Initialize table and event listeners on window load
window.onload = function() {
    populateSessionTable(sessions); // Populate table with initial data
    initializeSessionEventListeners(); // Set up event listeners
};

// Table interaction logic
const sessionDataTable = document.getElementById("session_table_view");
const sessionIDInput = document.getElementById("sessionID");
const sessionNameInput = document.getElementById("sessionName");

let sessionSelectedRow = null; // To track selected row


sessionDataTable.addEventListener("click", (event) => {
    const target = event.target;

    if (target && target.nodeName === "TD") {

        const updateBtn = document.getElementById("session_update_button");
        const deleteBtn = document.getElementById("session_delete_button");

       updateBtn.style.visibility = "visible";
       deleteBtn.style.visibility = "visible";
        const row = target.parentNode;

        if (sessionSelectedRow) {
            sessionSelectedRow.classList.remove("selected-row");
        }

        sessionSelectedRow = row;
        sessionSelectedRow.classList.add("selected-row");

        const sessionID = row.cells[0].textContent;
        const sessionName = row.cells[1].textContent;

        // Populate the form inputs with the selected row's data
        sessionIDInput.value = sessionID;
        sessionNameInput.value = sessionName;
    }

    event.stopPropagation();
});

// Handle click outside table to clear selection
document.addEventListener("click", (event) => {

    const isClickInsideTable = sessionDataTable.contains(event.target);
    const isClickInsideInputs = sessionIDInput.contains(event.target) || sessionNameInput.contains(event.target);

    if (!isClickInsideTable && !isClickInsideInputs && sessionSelectedRow) {

        const updateBtn = document.getElementById("session_update_button");
        const deleteBtn = document.getElementById("session_delete_button");
        sessionSelectedRow.classList.remove("selected-row");
        sessionSelectedRow = null;

        sessionIDInput.value = "";
        sessionNameInput.value = "";
        updateBtn.style.visibility = "hidden";
        deleteBtn.style.visibility = "hidden";
    }
});