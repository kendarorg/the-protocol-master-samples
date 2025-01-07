let username = '';

function sendData(path, verb, data, contentType, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open(verb, path);

    // Send the proper header information along with the request
    xhr.setRequestHeader("Content-Type", contentType);

    xhr.onreadystatechange = () => {
        // Call a function when the state changes.
        if (xhr.readyState === XMLHttpRequest.DONE) {
            callback(xhr.status, xhr.response);
            // Request finished. Do processing here.
        }
    };
    xhr.send(data);
}

function getData(path, verb, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open(verb, path);

    xhr.onreadystatechange = () => {
        // Call a function when the state changes.
        if (xhr.readyState === XMLHttpRequest.DONE) {
            callback(xhr.status, xhr.response);
            // Request finished. Do processing here.
        }
    };
    xhr.send();
}

function submitUsername(event) {
    if (event) event.preventDefault();
    username = document.getElementById('username').value.trim();
    chat = document.getElementById('chat');
    if (username) {
        document.getElementById('usernameInput').style.display = 'none';
        document.getElementById('chat').style.display = 'block';
        getData("/api/"+chat,"GET",(code, response) => {
            console.log("Loading messages for chat "+chat)
            if(code===200){
                const messages = document.getElementById('messages');
                let messagesToAppend = JSON.parse(response);
                for(var i=0;i<messagesToAppend.length;i++){
                    var toAppend = messagesToAppend[i];
                    const message = document.createElement('div');
                    message.textContent = toAppend.sender+": "+toAppend.message;
                    messages.appendChild(message);
                }
            }
            startWebSocket();
        })

    } else {
        alert('Please enter a valid username.');
    }
}

function startWebSocket() {
    const messages = document.getElementById('messages');
    const form = document.getElementById('form');
    const input = document.getElementById('input');
    const chatName = document.getElementById('chatName');

    const ws = new WebSocket('ws://localhost:8080/ws/'+chatName.value.trim());

    ws.onmessage = (event) => {
        const message = document.createElement('div');
        message.textContent = event.data;
        messages.appendChild(message);
    };

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const message = input.value.trim();
        if (message) {
            ws.send(username + ': ' + message);
            input.value = '';
        }
    });
}

// Add event listener for Enter key in the username input field
const usernameInput = document.getElementById('username');
usernameInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        submitUsername(event);
    }
});