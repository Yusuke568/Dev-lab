
// Check/uncheck all checkboxes
<<<<<<< HEAD
document.getElementById('select-all').addEventListener('change', function(e) {
=======
document.getElementById('select-all')?.addEventListener('change', function(e) {
>>>>>>> 5a6937d (feat: 勤怠管理機能の改善とUIの全面的な刷新)
    const checkboxes = document.querySelectorAll('input[name="selected_ids"]');
    checkboxes.forEach(cb => {
        cb.checked = e.target.checked;
    });
});

<<<<<<< HEAD
// Function for individual update
function updateIndividual(id) {
    const days = document.querySelector(`input[name="individual_days_${id}"]`).value;
    
    // Create a form dynamically to submit the data
    const form = document.createElement('form');
    form.method = 'post';
    form.action = 'UpdateLeaveServlet'; // This servlet needs to be created
=======
/**
 * Submits a POST request to update the leave days for a single employee.
 * @param {number|string} id The employee ID.
 * @param {number|string} days The new number of leave days.
 */
function postUpdate(id, days) {
    const contextPath = document.body.dataset.contextPath || '';
    // Create a form dynamically to submit the data
    const form = document.createElement('form');
    form.method = 'post';
    form.action = `${contextPath}/paid_leave/update.do`;
>>>>>>> 5a6937d (feat: 勤怠管理機能の改善とUIの全面的な刷新)

    const idInput = document.createElement('input');
    idInput.type = 'hidden';
    idInput.name = 'id';
    idInput.value = id;
    form.appendChild(idInput);

    const daysInput = document.createElement('input');
    daysInput.type = 'hidden';
    daysInput.name = 'days';
    daysInput.value = days;
    form.appendChild(daysInput);

    document.body.appendChild(form);
    form.submit();
}

<<<<<<< HEAD
// Make the function globally accessible from the JSP
window.updateIndividual = updateIndividual;
=======
// Add event listeners to all individual update buttons
document.querySelectorAll('.individual-update-btn').forEach(button => {
    button.addEventListener('click', (e) => {
        const row = e.target.closest('tr');
        if (!row) return;

        const employeeId = row.dataset.employeeId;
        const daysInput = row.querySelector('input[name="individual_days"]');
        
        if (employeeId && daysInput) {
            postUpdate(employeeId, daysInput.value);
        } else {
            console.error("Could not find employee ID or days input for the clicked button.", e.target);
        }
    });
});
>>>>>>> 5a6937d (feat: 勤怠管理機能の改善とUIの全面的な刷新)
