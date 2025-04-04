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
    return date.substring(0, 10) + " " + date.substring(11, 19);
}

function deepClone(obj) {
    return JSON.parse(JSON.stringify(obj));
}

function nextSymbolQuotation() {
    getData("/api/quotation/quote/" + params.symbol, "GET", (code, response) => {
        try {
            let ob = JSON.parse(response);
            chart.data.datasets.forEach((dataset) => {
                let label = dataset.label;
                let oldDataset = deepClone(dataset.data);
                let value = ob["price"];
                let time = parseDate(ob["date"]);
                if (label.localeCompare("Volume") === 0) {
                    value = ob["volume"];
                }
                let lastElement = oldDataset[oldDataset.length - 1];
                if (lastElement !== undefined && (lastElement.x + "").localeCompare(time + "") != 0) {
                    oldDataset.push({x: time, y: value});
                    changed = true;
                    dataset.data = oldDataset;
                } else if (lastElement === undefined) {
                    oldDataset.push({x: time, y: value});
                    changed = true;
                    dataset.data = oldDataset;
                }
            });
            if (changed) {
                chart.update();
            }
        } catch (error) {
            console.warn(error)
        }

        setTimeout(nextSymbolQuotation, 1000);
    });
}

let chart;

const params = new Proxy(new URLSearchParams(window.location.search), {
    get: (searchParams, prop) => searchParams.get(prop),
});

function initializeChart(elem) {
    const ctx = document.getElementById(elem);

    chart = new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "Price",
                    data: Array(),
                    yAxisID: 'prices',
                    fill: false,
                    borderColor: dynamicColors(),
                    type: "line"
                }, {
                    label: "Volume",
                    data: Array(),
                    yAxisID: 'volumes',
                    fill: false,
                    borderColor: dynamicColors(),
                    type: "bar"
                }
            ]
        },
        options: {
            responsive: true,
            adapters: {
                type: 'time',
                tyme: {
                    unit: 'second'
                }
            }
        }
    });
    setTimeout(nextSymbolQuotation, 1000);
}

var allSymbols = [];

function initializeQuotations(id) {
    const table = document.getElementById(id);
    getData("/api/quotation/symbols", "GET", (code, response) => {
        console.log("initialize quotations");
        try {
            symbolsArray = JSON.parse(response)
            if (symbolsArray.length == 0) throw new Error("No data yet");
            for (i = 0; i < symbolsArray.length; i++) {
                let symbol = symbolsArray[i]['symbol'];
                allSymbols.push(symbol);
                const row = document.createElement('tr');
                row.innerHTML = `
  <td>
    <a href="single.html?symbol=${symbol}">${symbol}</a>
  </td>
  <td>
  <span id="itemPrice${symbol}">NA</span>
  </td>
  <td>
  <span id="itemVolume${symbol}">NA</span>
  </td>
  <td>
  <span id="itemUpdate${symbol}">NA</span>
  </td>`;
                table.appendChild(row);
            }

        } catch (error) {
            console.warn(error)
            setTimeout(() => initializeQuotations(id), 1000);
            return;
        }
        setTimeout(nextQuotations, 1000);
    });
}

function nextQuotations() {
    for (var i = 0; i < allSymbols.length; i++) {
        let symbol = allSymbols[i];
        getData("/api/quotation/quote/" + symbol, "GET", (code, response) => {
            let ob = JSON.parse(response);
            let price = document.getElementById("itemPrice" + symbol);
            let volume = document.getElementById("itemVolume" + symbol);
            let update = document.getElementById("itemUpdate" + symbol);
            price.innerHTML = ob["price"];
            volume.innerHTML = ob["volume"];
            update.innerHTML = parseDate(ob["date"]);
        });
    }
    setTimeout(nextQuotations, 2000);
}