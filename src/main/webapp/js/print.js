document.addEventListener("DOMContentLoaded", function () {

    const pdf_chk_box = document.getElementById("pdf_chk_box");
    const excel_chk_box = document.getElementById("excel_chk_box");
    const room_check_box = document.getElementById("room_check_box");
    const teacher_check_box = document.getElementById("teacher_check_box");
    const semester_check_box = document.getElementById("semester_check_box");
    const printBtn = document.getElementById("print_button");

    document.getElementById("download_button").addEventListener("click", function () {
        fetch("/timetablexpert/downloadFolderServlet", { method: "GET" }) // Include the application context
            .then(response => {
                if (response.ok) {
                    // Trigger the download
                    window.location.href = "/timetablexpert/downloadFolderServlet"; // Ensure the same URL
                } else if (response.status === 404) {
                    alert("No file found to download. Please generate the timetable first.");
                } else {
                    alert("An error occurred while trying to download the file. Please try again.");
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Unable to connect to the server. Please try again later.");
            });
    });

       function showLoading() {
             const spinner = document.getElementById('circle-print');
                    const overlay = document.getElementById('overlay-print');
                    spinner.style.display = 'block'; // Show spinner
                    overlay.style.display = 'block'; // Show overlay
        }

        // Function to hide the loading spinner
        function hideLoading() {

            const spinner = document.getElementById('circle-print');
                    const overlay = document.getElementById('overlay-print');
                    spinner.style.display = 'none'; // Hide spinner
                    overlay.style.display = 'none'; // Hide overlay
        }
        function handlePrint(event) {
            event.preventDefault(); // Prevent form submission

            let typePDF = 0;
            let typeExcel = 0;
            let roomWise = 0;
            let teacherWise = 0;
            let departmentWise = 0;

            if (
                (!semester_check_box.checked &&
                 !teacher_check_box.checked &&
                 !room_check_box.checked) ||
                (!excel_chk_box.checked &&
                 !pdf_chk_box.checked)
            ) {
                alert("Please select the options to print.");
                return;
            }

            // Handle different combinations of selected formats and types
            if (excel_chk_box.checked && pdf_chk_box.checked) {
                typePDF = 1;
                typeExcel = 1;
                if (semester_check_box.checked) departmentWise = 1;
                if (teacher_check_box.checked) teacherWise = 1;
                if (room_check_box.checked) roomWise = 1;
            } else if (excel_chk_box.checked) {
                typeExcel = 1;
                if (semester_check_box.checked) departmentWise = 1;
                if (teacher_check_box.checked) teacherWise = 1;
                if (room_check_box.checked) roomWise = 1;
            } else if (pdf_chk_box.checked) {
                typePDF = 1;
                if (semester_check_box.checked) departmentWise = 1;
                if (teacher_check_box.checked) teacherWise = 1;
                if (room_check_box.checked) roomWise = 1;
            }

            const newPrint = {
                typePDF: typePDF,
                typeExcel: typeExcel,
                teacherWise: teacherWise,
                departmentWise: departmentWise,
                roomWise: roomWise
            };
                        showLoading();


            fetch('/timetablexpert/print-data', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newPrint)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to insert type timetable');
                }
                return response.json();  // The server can return a status response here
            })
            .then(data => {
                // Now fetch the zip file
                fetch("/timetablexpert/print-data")
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Failed to fetch zip file.");
                        }
                        return response.blob();  // Since we're expecting a binary file (ZIP)
                    })
                    .then(blob => {
                        const link = document.createElement('a');
                        link.href = URL.createObjectURL(blob);
                        link.download = 'TimeTable.zip'; // The file name for download
                        link.click();
                        hideLoading();
                        alert("Printed Successfully");
                    })
                    .catch(error => {
                        console.error("Error fetching zip data:", error);
                    });
            })
            .catch(error => console.error('Error timetable type:', error));
        }

    printBtn.addEventListener('click', handlePrint);
});
