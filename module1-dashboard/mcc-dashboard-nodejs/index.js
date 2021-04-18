const express = require("express");
const http = require("http");
const socketIo = require("socket.io");
const mqtt = require('mqtt');
const client = mqtt.connect('tcp://localhost:1883');
const app = express();
const server = http.createServer(app);
const io = socketIo(server);
const fileupload = require('express-fileupload');
const bodyparser = require('body-parser');
require('dotenv').config();
const topic_initiate_task = process.env.TOPIC_INITIATE_TASK
const topic_response = process.env.TOPIC_RESULT
let btoa = require('btoa');
app.use(fileupload());
app.use(bodyparser.json())
const port = process.env.PORT || 4000;

let reactSocket;

//Setup connection listener on MQTT and subscribe to send task and receive task result.
client.on('connect', function () {
    console.log('subscribed to the topics')
    client.subscribe(topic_initiate_task)
    client.subscribe(topic_response)
});

//On a successful connection, receives the messages for the subscribed topics.
client.on('message', function (topic, message) {

    let res_message;

    if(topic === topic_response) {
        res_message = getResult(message);
        console.log(res_message);
        getApiAndEmit(res_message);
    }
});

//Format the received task results.
function getResult(message) {

    let resultJson = JSON.parse(message);
    return "{\n" + 'Task: ' + resultJson.taskName + "\n" + "Result: " +resultJson.result + "\n}";
}

//Encode file content byte[] with base64 string.
function encode(data)
{
    let str = String.fromCharCode.apply(null,data);
    return btoa(str);
}

//API endpoint to receive task details from React and also publish tasks to the MQTT.
app.post('/postTask', (req, res) => {

    let taskDetailsObj = JSON.parse(req.body.taskDetails)
    let finalResult  = {};
    let taskResult = {
        "executableFile": {
            "name": req.files.executableFile.name,
            "data": encode(req.files.executableFile.data),
            "size": req.files.executableFile.size,
            "mimetype": req.files.executableFile.mimetype,
            "md5": req.files.executableFile.md5
        },
        "taskProperties": {
            "shortName": taskDetailsObj.shortName,
            "description": taskDetailsObj.description,
            "dueDate": taskDetailsObj.dueDate,
            "size": taskDetailsObj.size,
            "author": taskDetailsObj.author,
            "rewards": taskDetailsObj.rewards
        }
    };
    finalResult.task =taskResult;
    finalResult= JSON.stringify(finalResult);
    client.publish(topic_initiate_task, finalResult);
    console.log("Successfully submitted the task")
    res.send().status(200);
});

//Setup connection listener on react-node web socket.
io.on("connection", (socket) => {
    reactSocket = socket;
    console.log("New client connected");
});

//Emit or push a message or task results to the React.
const getApiAndEmit = (message) => {

    reactSocket.emit("FromAPI", message);
    console.log('Event emitted to the react app via sockets');
};

//Setup node app listener on the 4001 port. 
//Used server.listen to run socket.io within the same HTTP server instance.
server.listen(port, () => console.log(`Listening on port ${port}`));