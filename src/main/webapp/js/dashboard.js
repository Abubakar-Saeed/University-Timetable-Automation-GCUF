document.addEventListener('DOMContentLoaded', function () {
    const ctx = document.getElementById('polarChart');
    const barChart = document.getElementById('barChart');
    const teacherChart = document.getElementById('teacherChart');
    const roomChart = document.getElementById('roomChart');

    // Hardcoded colors for the first 5 entries
    const hardcodedColors = [
        'rgba(54,162,235,1)',
        'rgba(255,99,132,1)',
        'rgba(255,206,86,1)',
        'rgba(75,192,192,1)',
        'rgba(153,102,255,1)'
    ];

    // Function to generate additional random colors if needed
    function getColors(count) {
        const colors = [...hardcodedColors]; // Start with the hardcoded colors
        while (colors.length < count) {
            const r = Math.floor(Math.random() * 255);
            const g = Math.floor(Math.random() * 255);
            const b = Math.floor(Math.random() * 255);
            colors.push(`rgba(${r}, ${g}, ${b}, 0.8)`);
        }
        return colors.slice(0, count); // Ensure the returned array matches the required count
    }

    fetch('/timetablexpert/dashboard-data') // Use the correct context path here
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Fetched Data:', data); // Debug: Log entire fetched data

            // Update DOM elements with fetched data
            document.getElementById("programCount").innerText = data.statsInfo.totalPrograms;
            document.getElementById("classesCount").innerText = data.statsInfo.totalClasses;
            document.getElementById("rCount").innerText = data.statsInfo.totalRegularTeachers;
            document.getElementById("vCount").innerText = data.statsInfo.totalVisitingTeachers;

            // Extract program names and counts for the charts
            let programNames = data.programs.map(program => program.programName);
            let programCounts = data.programs.map(program => program.count);
            const dynamicColors = getColors(programNames.length);

            // Debugging logs for Polar Area Chart
            console.log('Polar Area Chart:');
            console.log('Program Names:', programNames);
            console.log('Program Counts:', programCounts);
            console.log('Colors Used:', dynamicColors);

            // Update Polar Area Chart for Class Distribution
            new Chart(ctx, {
                type: 'polarArea',
                data: {
                    labels: programNames,
                    datasets: [{
                        label: 'Class Distribution',
                        data: programCounts,
                        backgroundColor: dynamicColors,
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                }
            });

            programNames = data.regularTeachers.map(teacher => teacher.programName);
            programCounts = data.regularTeachers.map(teacher => teacher.count);

            // Debugging logs for Bar Chart
            console.log('Bar Chart - Regular Teachers:');
            console.log('Program Names:', programNames);
            console.log('Program Counts:', programCounts);

            // Update Bar Chart for Regular Teachers Overview
            new Chart(barChart, {
                type: 'bar',
                data: {
                    labels: programNames,
                    datasets: [{
                        label: 'Regular Teachers Overview',
                        data: programCounts,
                        backgroundColor: getColors(programNames.length),
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                }
            });

            programNames = data.visitingTeachers.map(teacher => teacher.programName);
            programCounts = data.visitingTeachers.map(teacher => teacher.count);

            // Debugging logs for Doughnut Chart


            // Update Doughnut Chart for Visiting Teachers Overview
            new Chart(teacherChart, {
                type: 'doughnut',
                data: {
                    labels: programNames,
                    datasets: [{
                        label: 'Visiting Teachers Overview',
                        data: programCounts,
                        backgroundColor: getColors(programNames.length),
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                }
            });

            programNames = data.rooms.map(room => room.programName);
            programCounts = data.rooms.map(room => room.count);

            // Debugging logs for Pie Chart
            console.log('Pie Chart - Rooms:');
            console.log('Program Names:', programNames);
            console.log('Program Counts:', programCounts);

            // Update Pie Chart for Rooms Overview
            new Chart(roomChart, {
                type: 'bar',
                data: {
                    labels: programNames,
                    datasets: [{
                        label: 'Rooms Overview',
                        data: programCounts,
                        backgroundColor: getColors(programNames.length),
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                }
            });
        })
        .catch(error => console.error('Fetch error:', error));
});
