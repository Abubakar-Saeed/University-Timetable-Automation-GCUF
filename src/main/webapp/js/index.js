// Toggle menu functionality
document.addEventListener('DOMContentLoaded', () => {
    const toggleIcon = document.querySelector('.toggle-icon');
    const navLinks = document.querySelector('.nav-links');

    toggleIcon.addEventListener('click', () => {
        // Toggle the "show" class on nav-links to show/hide the menu
        navLinks.classList.toggle('show');

        // Change the toggle icon
        const menuIcon = toggleIcon.querySelector('ion-icon');
        if (menuIcon.name === 'menu-outline') {
            menuIcon.name = 'close-outline'; // Change to close icon
        } else {
            menuIcon.name = 'menu-outline'; // Revert to menu icon
        }
    });




});
