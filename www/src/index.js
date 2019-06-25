'use strict';

require("./styles.scss");

const {Elm} = require('./Main');
const app = Elm.Main.init({flags: []});

var webSocket;

function connect() {
    let ws = new WebSocket('wss://api-ws.resilient-demo.symphonia.io');
    ws.onopen = wsOnOpen;
    ws.onclose = wsOnClose;
    ws.onerror = wsOnError;
    ws.onmessage = wsOnMessage;
    return ws;
}

function wsOnOpen() {
    console.log('connected');
}

function wsOnClose() {
    console.log('disconnected');
}

function wsOnError(err) {
    console.log('error: ' + JSON.stringify(err));
    // Attempt to reconnect
    new Promise(resolve => setTimeout(resolve, 1000))
        .then(() => {
            console.log('attempting to reconnect');
            webSocket = connect();
        });
}

function wsOnMessage(event) {
    console.log('inbound: ' + event.data);
    app.ports.inbound.send(JSON.parse(event.data));
}

function send(i, message) {
    if (webSocket.readyState === WebSocket.OPEN) {
        webSocket.send(JSON.stringify(message));
    } else if (i < 2) {
        webSocket = connect();
        send(i++, message);
    }
}

webSocket = connect();

app.ports.outbound.subscribe(message => {
    console.log('outbound: ' + JSON.stringify(message));
    send(0, message);
});
