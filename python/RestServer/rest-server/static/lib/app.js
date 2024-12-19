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

function dynamicColors() {
    var r = Math.floor(Math.random() * 255);
    var g = Math.floor(Math.random() * 255);
    var b = Math.floor(Math.random() * 255);
    return "rgb(" + r + "," + g + "," + b + ")";
}

function parseDate(date) {
    return date.substring(0,10)+" "+date.substring(11,19);
}

function getDataLoop(prices, volumes, times, symbols, callback) {
    if (symbols.length == 0) {
        callback();
        return;
    }
    var last = symbols.pop();
    getData("/api/quotation/quote/" + last['symbol'], "GET", (code, response) => {
        let ob = JSON.parse(response);
        prices[last['symbol']] = ob["price"];
        volumes[last['symbol']] = ob["volume"];
        times[last['symbol']] = parseDate(ob["date"]);
        getDataLoop(prices, volumes, times, symbols, callback)
    });
}

function nextNotice() {
    var prices = []
    var volumes = []
    var times = []
    var symbols = JSON.parse(JSON.stringify(symbolsArray));
    getDataLoop(prices, volumes, times, symbols, () => {
        let changed = false;
        chart.data.datasets.forEach((dataset) => {
            let label = dataset.label;
            let price = prices[label];
            let time = (times[label]);
            let lastElement = dataset.data[dataset.data.length - 1];
            if (lastElement !== undefined && (lastElement.x+"").localeCompare(time+"") != 0) {
                dataset.data.push({x: time, y: price});
                changed = true;
            } else if (lastElement === undefined) {
                dataset.data.push({x: time, y: price});
                changed = true;
            }
        });
        if (changed) chart.update('none');
        setTimeout(nextNotice, 1000);
    });
}

let symbolsArray = Array();
let chart;

function initializeChart(elem) {
    const ctx = document.getElementById(elem);

    getData("/api/quotation/symbols", "GET", (code, response) => {
        symbolsArray = JSON.parse(response)
        var datasets = Array();
        for (i = 0; i < symbolsArray.length; i++) {
            var dataset = {
                label: symbolsArray[i]['symbol'],
                data: Array(),
                fill: false,
                borderColor: dynamicColors()
            }
            datasets.push(dataset)
        }
        chart = new Chart(ctx, {
            type: 'line',
            data: {
                datasets: datasets
            },
            options: {
                responsive: true,
                adapters: {
                    type: 'time',
                    tyme:{
                        unit:'second'
                    }
                }
            }
        });
        setTimeout(nextNotice, 1000);
    })
}