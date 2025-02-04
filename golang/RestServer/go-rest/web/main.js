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
    let chatName = document.getElementById('chatName').value.trim();
    let chat = document.getElementById('chat');
    if (username) {
        document.getElementById('usernameInput').style.display = 'none';
        document.getElementById('chat').style.display = 'block';
        getData("/api/"+chatName,"GET",(code, response) => {
            console.log("Loading messages for chat "+chatName)
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

let localWebSocket = null;

function trySend(counter,message){
    if(counter<0){
        console.log("timeout reached for "+message)
    }
    try {
        console.log("trySend "+message)
        localWebSocket.send(message);
        console.log("trySendSucceeded "+message)
    } catch (error) {
        console.error(error);
        counter--;
        setTimeout(()=>{
            trySend(counter,message);
        }, 1000)
    }
}

function startWebSocket() {
    console.log("startWebSocket")
    const messages = document.getElementById('messages');
    const form = document.getElementById('form');
    const input = document.getElementById('input');
    const chatName = document.getElementById('chatName');

    const domain = window.location.hostname;
    const port = window.location.port;

    localWebSocket = new WebSocket('ws://'+domain+':'+port+'/ws/'+chatName.value.trim());
    var eventListener = (event) => {
        event.preventDefault();
        const message = input.value.trim();
        if (message) {
            let toSend = username + ': ' + message;
            var counter = 10;
            input.value = '';
            trySend(counter,toSend);
        }
    }
    localWebSocket.onmessage = (event) => {
        console.log("startWebSocket::onmessage")
        const message = document.createElement('div');
        message.textContent = event.data;
        messages.appendChild(message);
    };

    localWebSocket.onclose =  (event) => {
        console.log("startWebSocket::onclose")
        localWebSocket = null;
        try {
            form.removeEventListener('submit', eventListener)
        } catch (error) {
            console.error(error);
            // Expected output: ReferenceError: nonExistentFunction is not defined
            // (Note: the exact output may be browser-dependent)
        }
        // connection closed, discard old websocket and create a new one in 5s
        setTimeout(startWebSocket, 1000)
    }

    localWebSocket.onerror =  (event) => {
        console.log("startWebSocket::onerror")
        try {
            // connection closed, discard old websocket and create a new one in 5s
            localWebSocket.close()
            form.removeEventListener('submit', eventListener)
        } catch (error) {
            console.error(error);
            // Expected output: ReferenceError: nonExistentFunction is not defined
            // (Note: the exact output may be browser-dependent)
        }
    }

    form.addEventListener('submit', eventListener);
}

// Add event listener for Enter key in the username input field
const usernameInput = document.getElementById('username');
usernameInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        submitUsername(event);
    }
});