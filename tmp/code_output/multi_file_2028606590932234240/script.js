const taskForm = document.getElementById('taskForm');
const todoList = document.getElementById('todoList');
const inProgressList = document.getElementById('inProgressList');
const doneList = document.getElementById('doneList');

function allowDrop(event) {
    event.preventDefault();
}

function drag(event) {
    event.dataTransfer.setData('text', event.target.id);
}

function drop(event, column) {
    event.preventDefault();
    const data = event.dataTransfer.getData('text');
    const task = document.getElementById(data);
    if (column === 'todo') {
        todoList.appendChild(task);
    } else if (column === 'inProgress') {
        inProgressList.appendChild(task);
    } else if (column === 'done') {
        doneList.appendChild(task);
    }
}

taskForm.addEventListener('submit', function(event) {
    event.preventDefault();
    const title = document.getElementById('taskTitle').value;
    const priority = document.getElementById('taskPriority').value;
    const column = document.getElementById('taskColumn').value;

    const task = document.createElement('div');
    task.className = 'task';
    task.id = 'task_' + Date.now();
    task.draggable = true;
    task.ondragstart = drag;
    task.innerHTML = `<span class='priority ${priority}'>${priority.charAt(0).toUpperCase() + priority.slice(1)}</span> ${title}<button onclick='deleteTask(this)'>Delete</button>`;

    if (column === 'todo') {
        todoList.appendChild(task);
    } else if (column === 'inProgress') {
        inProgressList.appendChild(task);
    } else if (column === 'done') {
        doneList.appendChild(task);
    }

    taskForm.reset();
});

function deleteTask(button) {
    const task = button.parentElement;
    task.remove();
}