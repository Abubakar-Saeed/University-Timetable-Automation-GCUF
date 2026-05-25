document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.querySelector("#admin_table_view tbody");
    const addAdminBtn = document.getElementById("add_btn_user");
    const formModel = document.getElementById("addAdminModal");
    const cancelForm = document.getElementById("close"); // Used to close the modal
    const form = document.getElementById("addAdminForm");
    const submitBtn = document.getElementById("submit_btn");

    console.log(submit_btn);
    let isUpdating = false; // Flag to track if we're updating
    let currentAdminID = null;

    cancelForm.onclick = function () {
        formModel.style.display = "none"; // Close the modal
        resetForm(); // Clear form content
    };

    // Fetch and populate admin data
    function fetchAdminData() {
        fetch('/timetablexpert/register-data')
            .then((response) => response.json())
            .then((data) => {
                const admins = data.registerData;
                tableBody.innerHTML = ""; // Clear table before appending
                if (admins.length === 0) {
                    tableBody.innerHTML = `<tr><td colspan="4" class="no-data">No Admins Found</td></tr>`;
                } else {
                    admins.forEach((admin, index) => {
                        const row = document.createElement("tr");
                        row.innerHTML = `
                            <td>${index + 1}.</td>
                            <td>${admin.userName}</td>
                            <td>*******</td>
                            <td>
                                <button class="update-btn" data-id="${admin.userID}" data-username="${admin.userName}" data-password="${admin.password}">Update</button>
                                <button class="delete-btn" data-id="${admin.userID}">Delete</button>
                            </td>
                        `;
                        tableBody.appendChild(row);
                    });
                }
            })
            .catch((error) => console.error("Error fetching admin data:", error));
    }

    // Add admin logic
    addAdminBtn.addEventListener("click", function (e) {
        e.preventDefault();
        isUpdating = false; // Set to adding mode
                    submit_btn.value = "Add Admin";

        formModel.style.display = "block"; // Show the modal
        resetForm(); // Clear any pre-filled data
    });

    // Handle form submission
    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const formData = new FormData(form); // Collect form data

        if (isUpdating) {
            // Update Admin logic
            fetch(`/timetablexpert/register-data/${currentAdminID}`, {
                method: "PUT",
                body: formData,

                })

                .then((response) => response.json())
                .then((result) => {
                    alert(result.message || "Admin updated successfully");
                    fetchAdminData();
                    formModel.style.display = "none"; // Close the modal
                    resetForm(); // Clear form data
                })
                .catch((error) => console.error("Error updating admin:", error));
        } else {

        console.log("Here in post Method");

        if (!formData.get('image')) {
            alert('Please select an image.');
            return;
        }
            // Add Admin logic
            fetch('/timetablexpert/register-data', {
                method: "POST",
                body: formData,
            })
                .then((response) => response.json())
                .then((result) => {
                    alert(result.message || "Admin added successfully");
                    fetchAdminData();
                    formModel.style.display = "none"; // Close the modal
                    resetForm(); // Clear form data
                })
                .catch((error) => console.error("Error adding admin:", error));
        }
    });

    // Delegate event listeners for Update and Delete
    tableBody.addEventListener("click", function (e) {
        const target = e.target;

        if (target.classList.contains("delete-btn")) {
            const adminID = target.dataset.id;
            if (confirm("Are you sure you want to delete this admin?")) {
                fetch(`/timetablexpert/register-data/${adminID}`, { method: "DELETE" })
                    .then((response) => response.json())
                    .then((result) => {
                        alert(result.message || "Admin deleted successfully");
                        fetchAdminData();
                    })
                    .catch((error) => console.error("Error deleting admin:", error));
            }
        }

        if (target.classList.contains("update-btn")) {
            submit_btn.value = "Update Admin";
            isUpdating = true; // Set to updating mode
            currentAdminID = target.dataset.id; // Store the admin ID
            const username = target.dataset.username;
            const password = target.dataset.password;

            // Populate the form with current admin data
            document.getElementById("userName").value = username;
            document.getElementById("password").value = password;

            formModel.style.display = "block"; // Show the modal
        }
    });

    // Clear form inputs
    function resetForm() {
        form.reset(); // Clear all form inputs
        currentAdminID = null;
        isUpdating = false;
    }

    // Initial fetch of admin data
    fetchAdminData();
});
