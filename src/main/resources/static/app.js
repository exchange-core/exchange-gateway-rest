var stompClient = null;

var connectFlag = false;

function setConnected(connected) {
    connectFlag = connected;
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/ticks-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/notifications', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
//        stompClient.subscribe('/topic/ticks', function (tick) {
//            showTick(JSON.parse(tick.body));
//        });

    });
}

function subscribe() {
    if (connectFlag) {
        var symbolToSubscribe = $("#symbol").val()
        console.log('Subscribing: ' + symbolToSubscribe);
        stompClient.subscribe('/topic/ticks/' + symbolToSubscribe, function (tick) {
            showTick(JSON.parse(tick.body));
        });
    }
}

function unsubscribe() {
    if (connectFlag) {
        var symbolToSubscribe = $("#symbol").val()
        console.log('UnSubscribing: ' + symbolToSubscribe);
        stompClient.unsubscribe('/topic/ticks/' + symbolToSubscribe);
    }
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

function showTick(tick) {
    $("#messages").append("<tr><td>" + tick.timestamp + " " + tick.price + " " + tick.size +"</td></tr>");
}


$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( "#subscribe" ).click(function() { subscribe(); });
    $( "#unsubscribe" ).click(function() { unsubscribe(); });
});
