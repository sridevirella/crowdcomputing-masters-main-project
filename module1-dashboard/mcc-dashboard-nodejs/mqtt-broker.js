let mosca = require('mosca');
let settings = {
    port: 1883
}

//Setup mosca Mqtt server
let server = new mosca.Server(settings);
server.on('ready', function () {
    console.log("ready");
});