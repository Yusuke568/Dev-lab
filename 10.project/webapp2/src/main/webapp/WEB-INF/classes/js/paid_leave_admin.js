
// Check/uncheck all checkboxes
document.getElementById('select-all').addEventListener('change', function(e) {
    const checkboxes = document.querySelectorAll('input[name="selected_ids"]');
    checkboxes.forEach(cb => {
        cb.checked = e.target.checked;
    });
});

// Function for individual update
function updateIndividual(id) {
    const days = document.querySelector(`input[name="individual_days_${id}"]`).value;
    
    // Create a form dynamically to submit the data
    const form = document.createElement('form');
    form.method = 'post';
    form.action = 'UpdateLeaveServlet'; // This servlet needs to be created

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

// Make the function globally accessible from the JSP
window.updateIndividual = updateIndividual;
