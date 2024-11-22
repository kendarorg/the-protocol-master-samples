$(document).ready(onReady);

var apiServerAddress = "";

// on page load, makes GET request for tasks table in database and applies event listeners
function onReady() {
    $.ajax({
        method: 'GET',
        url: '/apiserver'
    }).then(function (response) {
        console.log("Server address is: " + response);
        apiServerAddress = response;
        getTasks();
        applyEventListeners();
    }).catch(function (error) {
        console.log('Error in client-side GET request to /api/tasks', error);
    });
}

// adds ajax requests as event listeners on submit button and delete, archive and update buttons in table tasks
function applyEventListeners() {
    $("#submitNewTask").on('click', handleNewTask);
    $("#setActiveTasks").on('click', changeMode);
    $("#setArchivedTasks").on('click', changeMode);
    $('#listTableBody').on('click', '.deleteButton', handleDeleteTask);
    $('#archivedTableBody').on('click', '.deleteButton', handleDeleteTask);
    $('#listTableBody').on('click', '.updateButton', handleUpdateTask);
    $('#listTableBody').on('click', '.archiveButton', handleArchiveTask);
}

// GETs tasks table row contents from database
function getTasks() {
    $.ajax({
        method: 'GET',
        url: apiServerAddress + '/api/tasks',
        crossDomain: true
    }).then(function (response) {
        appendTasks(response);
    }).catch(function (error) {
        console.log('Error in client-side GET request to /api/tasks', error);
    });
}

// appends rows to array
function appendTasks(array) {
    // empties previous table contents before update
    $("#listTableBody").empty();
    $("#archivedTableBody").empty();
    // incrementers for table row numbers
    let taskRowCount = 1;
    let archivedRowCount = 1;
    for (task of array) {
        // if task is completed, adds completedTask class and removes update button functionality
        if (task.status === "Completed") {
            $('#listTableBody').append(`
            <tr data-id="${task.id}">
                <th scope="row">${taskRowCount}</th>
                <td>${task.taskName}</td>
                <td>${task.priority}</td>
                <td>${task.status}</td>
                <td>${task.notes}</td>
                <td><button class="deleteButton btn btn-danger btn-sm">Delete</button></td>
                <td><button class="archiveButton btn btn-outline-success btn-sm">Archive</button></td>
            </tr>
            `)
            $("#listTableBody").children().last().addClass("completedTask");
            taskRowCount++;
            // if task is archived, adds to archived tasks table
        } else if (task.status === "Archived") {
            $('#archivedTableBody').append(`
            <tr data-id="${task.id}">
                <th scope="row">${archivedRowCount}</th>
                <td>${task.taskName}</td>
                <td>${task.priority}</td>
                <td>${task.notes}</td>
                <td>${task.archiveDate.substring(0, 10)}</td>
                <td><button class="deleteButton btn btn-danger btn-sm">Delete</button></td>
            </tr>
            `)
            archivedRowCount++;
            // if task is neither archived nor completed, adds task to task table with update and delete buttons
        } else {
            $('#listTableBody').append(`
            <tr data-id="${task.id}">
                <th scope="row">${taskRowCount}</th>
                <td>${task.taskName}</td>
                <td>
                    <select class="selectPriority">
                        <option value="${task.priority}" disabled selected hidden>${task.priority}</option>
                        <option value = "High">High</option>
                        <option value = "Medium">Medium</option>
                        <option value = "Low">Low</option>
                    </select>
                </td>
                <td>
                    <select class="selectStatus">
                        <option value="${task.status}" disabled selected hidden>${task.status}</option>
                        <option value = "Not Started">Not Started</option>
                        <option value = "In Progress">In Progress</option>
                        <option value = "Completed">Completed</option>
                    </select>
                </td>
                <td>${task.notes}</td>
                <td><button class="deleteButton btn btn-danger btn-sm">Delete</button></td>
                <td><button class="updateButton btn btn-outline-primary btn-sm">Update</button></td>
            </tr>
            `)
            taskRowCount++;
        }
    }
}

function changeMode() {
    if ($(this).text() === "Active") {
        $("#active-tasks").removeClass("hidden");
        $('#archived-tasks').addClass("hidden");
    } else if ($(this).text() === "Archive") {
        $("#archived-tasks").removeClass("hidden");
        $('#active-tasks').addClass("hidden");
    }
}

// on Submit button click, adds new task to database, then GETs contents to append to DOM
function handleNewTask() {
    // requires taskName and Notes inputs to have values
    if (!$("#taskName").val() || !$("#addPriority").val()) {
        return Swal.fire({
            type: 'error',
            title: 'Oops...',
            text: 'Something went wrong!',
            footer: 'Please enter task name and priority.'
        })
    }
    // posts new task to database with input values
    $.ajax({
        method: 'POST',
        url: apiServerAddress + '/api/tasks',
        data: JSON.stringify({
            taskName: $("#taskName").val(),
            priority: $("#addPriority").val(),
            notes: $("#notes").val()
        }),
        crossDomain: true,
        contentType: "application/json"
        // updates the task list and empties input fields
    }).then(function () {
        getTasks();
        $("#taskName").val("");
        $("#addPriority").val("");
        $("#notes").val("");
    }).catch(function (error) {
        console.log('Error in client-side POST', error);
    });
}

// on delete button click, removes that task from database and GETs contents to append to DOM
function handleDeleteTask() {
    let id = $(this).parent().parent().data().id;
    console.log("Delete activated");
    // sweet alerts modal for choosing to confirm delete
    Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        type: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!'
        // modal for displaying deletiong success
    }).then((result) => {
        if (result.value) {
            Swal.fire(
                'Deleted!',
                'Your file has been deleted.',
                'success'
            )
            // deletes task from database
            $.ajax({
                method: 'DELETE',
                url: apiServerAddress + "/api/tasks/" + id,
                crossDomain: true
            }).then(function () {
                getTasks();
            }).catch(function (error) {
                console.log('Error in Client-side Delete Request', error);
            });
        }
    })
}

// on update button click, alters that item's priority and status in database , then updates table contents on DOM
function handleUpdateTask() {
    // grabs id from table row
    let id = $(this).parent().parent().data().id;
    // grabs selected status from status dropdown
    let status = $(this).parent().parent().find(".selectStatus").children("option:selected").val();
    // grabs selected priority from priority dropdown
    let priority = $(this).parent().parent().find(".selectPriority").children("option:selected").val();

    let notes = $(this).parent().parent().data().notes;
    console.log(`status to update item ${id} to:`, status);
    console.log(`priority to update item ${id} to:`, priority);
    $.ajax({
        method: 'PUT',
        url: apiServerAddress + "/api/tasks/" + id,
        data: JSON.stringify({
            id: id,
            status: status,
            priority: priority
        }),
        crossDomain: true,
        contentType: "application/json"
    }).then(function () {
        // updates table
        getTasks();
    }).catch(function (error) {
        console.log('There is an error in client-side PUT request', error)
    });
}

// on archive button click, changes Status in database to Archived , then updates table contents on DOM
function handleArchiveTask() {
    // grabs id from table row
    let id = $(this).parent().parent().data().id;
    console.log(`status to update item ${id} to: "Archived"`);
    // updates status of task to "Archived".
    $.ajax({
        method: 'PUT',
        url: apiServerAddress + "/api/tasks/archive/" + id,
        crossDomain: true,
        contentType: "application/json"
    }).then(function () {
        // updates tables
        getTasks();
    }).catch(function (error) {
        console.log('There is an error in client-side PUT request', error)
    });
}
