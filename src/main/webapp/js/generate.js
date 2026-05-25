let generateData = []; // Array to store the fetched data
const generateTableBody = document.querySelector("#generate_table_view tbody");
const generateSearchInput = document.getElementById('search-input');
const generateButton = document.getElementById('generate_btn');
const resetButton = document.getElementById('reset_btn');
const regenerateButton = document.getElementById('regenerate_btn');




document.addEventListener("DOMContentLoaded", function () {

   function showLoading() {
         const spinner = document.getElementById('circle');
                const overlay = document.getElementById('overlay');
                spinner.style.display = 'block'; // Show spinner
                overlay.style.display = 'block'; // Show overlay
    }

    // Function to hide the loading spinner
    function hideLoading() {

        const spinner = document.getElementById('circle');
                const overlay = document.getElementById('overlay');
                spinner.style.display = 'none'; // Hide spinner
                overlay.style.display = 'none'; // Hide overlay
    }

    initializeGenerateEventListener();
    // Fetch data from the server and store it in an array
    function fetchTableData() {
        fetch("/timetablexpert/generate-data")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch data");
                }
                return response.json();
            })
            .then(data => {
                generateData = data.generateData.map(generate => ({
                                               timetableID: generate.timetableID,
                                               day: generate.day,
                                               slot: generate.slot,
                                               slotTitle: generate.slotTitle,

                                           }));


                populateGenerateTable(generateData); // Populate the table with all data
                                                                       console.log("Generate Data: ",generateData);

            })
            .catch(error => {
                console.error("Error fetching table data:", error);
            });
    }

    // Populate the table with data
    function populateGenerateTable(data) {
        generateTableBody.innerHTML = ""; // Clear the table body
        data.forEach(item => {
            const row = createTableRow(item);
            generateTableBody.appendChild(row);
        });
    }

    // Create a table row from an item
    function createTableRow(item) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${item.timetableID}</td>
            <td>${item.day}</td>
            <td>${item.slot}</td>
            <td>${item.slotTitle}</td>
        `;
        return row;
    }

    // Search the stored data based on the input value
    function searchGenerateTable(value, data) {
        const searchValue = value.toLowerCase();
        return data.filter(item => (
            item.timetableID.toString().includes(searchValue) ||
            item.day.toLowerCase().includes(searchValue) ||
            item.slot.toLowerCase().includes(searchValue) ||
            item.slotTitle.toLowerCase().includes(searchValue)
        ));
    }

    // Handle the search input
    function handleGenerateSearch() {
        const value = this.value; // Get the search input value
        const filteredData = searchGenerateTable(value, generateData); // Filter the stored data
        populateGenerateTable(filteredData); // Repopulate the table with filtered data
    }

    if (generateSearchInput) {
        generateSearchInput.addEventListener("input", handleGenerateSearch);
    }

    function initializeGenerateEventListener(){


       generateButton.addEventListener('click',generateTimetable);
       regenerateButton.addEventListener('click',generateTimetable);
       resetButton.addEventListener('click',reset);

    }

    // Fetch data when the DOM content is loaded
    fetchTableData();


    function reset(){

    generateTableBody.innerHTML = "";

    }

function generateTimetable() {
    showLoading();

    fetch('/timetablexpert/generate-data?action=generate')
        .then(response => {

            return response.text();  // If expecting plain text
            // Or use response.json() if the server returns JSON data
        })
        .then(data => {

            hideLoading();
            fetchTableData();
            alert(data);

            // Optionally, you can process the 'data' if needed
            console.log(data); // To view the returned text
        })
        .catch(error => {
            hideLoading();
            console.error('Error fetching data:', error);
            alert(`Error: ${error.message}`);
        });
}


});
